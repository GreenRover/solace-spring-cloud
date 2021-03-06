package com.solace.spring.cloud.stream.binder.inbound;

import com.solace.spring.cloud.stream.binder.util.SolaceMessageConversionException;
import com.solace.spring.cloud.stream.binder.util.SolaceMessageHeaderErrorMessageStrategy;
import com.solace.spring.cloud.stream.binder.util.XMLMessageMapper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ClosedFacilityException;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPTransportException;
import com.solacesystems.jcsmp.XMLMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.core.AttributeAccessor;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AckUtils;
import org.springframework.integration.support.ErrorMessageUtils;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class InboundXMLMessageListener implements Runnable {
	final FlowReceiver flowReceiver;
	final ConsumerDestination consumerDestination;
	final ThreadLocal<AttributeAccessor> attributesHolder;
	private final XMLMessageMapper xmlMessageMapper = new XMLMessageMapper();
	private final Consumer<Message<?>> messageConsumer;
	private final Function<RuntimeException,Boolean> errorHandlerFunction;
	private final boolean needHolder;
	private final boolean needAttributes;
	private final AtomicBoolean stopFlag = new AtomicBoolean(false);
	private final Supplier<Boolean> remoteStopFlag;

	private static final Log logger = LogFactory.getLog(InboundXMLMessageListener.class);

	InboundXMLMessageListener(FlowReceiver flowReceiver,
							  ConsumerDestination consumerDestination,
							  Consumer<Message<?>> messageConsumer,
							  Function<RuntimeException,Boolean> errorHandlerFunction,
							  @Nullable AtomicBoolean remoteStopFlag,
							  ThreadLocal<AttributeAccessor> attributesHolder,
							  boolean needHolderAndAttributes) {
		this(flowReceiver, consumerDestination, messageConsumer, errorHandlerFunction, remoteStopFlag, attributesHolder, needHolderAndAttributes, needHolderAndAttributes);
	}

	InboundXMLMessageListener(FlowReceiver flowReceiver,
							  ConsumerDestination consumerDestination,
							  Consumer<Message<?>> messageConsumer,
							  Function<RuntimeException,Boolean> errorHandlerFunction,
							  @Nullable AtomicBoolean remoteStopFlag,
							  ThreadLocal<AttributeAccessor> attributesHolder,
							  boolean needHolder,
							  boolean needAttributes) {
		this.flowReceiver = flowReceiver;
		this.consumerDestination = consumerDestination;
		this.messageConsumer = messageConsumer;
		this.errorHandlerFunction = errorHandlerFunction;
		this.remoteStopFlag = () -> remoteStopFlag != null && remoteStopFlag.get();
		this.attributesHolder = attributesHolder;
		this.needHolder = needHolder;
		this.needAttributes = needAttributes;
	}

	@Override
	public void run() {
		try {
			while (keepPolling()) {
				try {
					receive();
				} catch (RuntimeException e) {
					// Shouldn't ever come in here.
					// Doing this just in case since the message consumers shouldn't ever stop unless interrupted.
					logger.warn(String.format("Exception received while consuming messages from destination %s",
							consumerDestination.getName()), e);
				}
			}
		} finally {
			logger.info(String.format("Closing flow receiver to destination %s", consumerDestination.getName()));
			flowReceiver.close();
		}
	}

	private boolean keepPolling() {
		return !stopFlag.get() && !remoteStopFlag.get();
	}

	public void receive() {
		BytesXMLMessage bytesXMLMessage;

		try {
			bytesXMLMessage = flowReceiver.receive();
		} catch (JCSMPException e) {
			String msg = String.format("Received error while trying to read message from endpoint %s",
					flowReceiver.getEndpoint().getName());
			if ((e instanceof JCSMPTransportException || e instanceof ClosedFacilityException) && !keepPolling()) {
				logger.debug(msg, e);
			} else {
				logger.warn(msg, e);
			}
			return;
		}

		if (bytesXMLMessage == null) {
			return;
		}

		try {
			final Message<?> message = xmlMessageMapper.map(bytesXMLMessage);
			handleMessage(message, bytesXMLMessage);
		} catch (SolaceMessageConversionException e) {
			handleError(e, bytesXMLMessage, bytesXMLMessage::ackMessage);
		} finally {
			if (needHolder) {
				attributesHolder.remove();
			}
		}
	}

	void handleMessage(final Message<?> message, BytesXMLMessage bytesXMLMessage) {
		try {
			sendToConsumer(message, bytesXMLMessage);
			AckUtils.autoAck(StaticMessageHeaderAccessor.getAcknowledgmentCallback(message));
		} catch (RuntimeException e) {
			handleError(e, bytesXMLMessage, () -> AckUtils.autoNack(StaticMessageHeaderAccessor.getAcknowledgmentCallback(message)));
		}
	}

	void sendToConsumer(final Message<?> message, final BytesXMLMessage bytesXMLMessage) throws RuntimeException {
		setAttributesIfNecessary(bytesXMLMessage, message);
		AtomicInteger deliveryAttempt = StaticMessageHeaderAccessor.getDeliveryAttempt(message);
		if (deliveryAttempt != null) {
			deliveryAttempt.incrementAndGet();
		}
		messageConsumer.accept(message);
	}

	void setAttributesIfNecessary(XMLMessage xmlMessage, org.springframework.messaging.Message<?> message) {
		if (needHolder) {
			attributesHolder.set(ErrorMessageUtils.getAttributeAccessor(null, null));
		}

		if (needAttributes) {
			AttributeAccessor attributes = attributesHolder.get();
			if (attributes != null) {
				attributes.setAttribute(SolaceMessageHeaderErrorMessageStrategy.INPUT_MESSAGE, message);
				attributes.setAttribute(SolaceMessageHeaderErrorMessageStrategy.SOLACE_RAW_MESSAGE, xmlMessage);
			}
		}
	}

	private void handleError(RuntimeException e, BytesXMLMessage bytesXMLMessage, Runnable acknowledgement) {
		setAttributesIfNecessary(bytesXMLMessage, null);
		boolean wasProcessedByErrorHandler = errorHandlerFunction != null && errorHandlerFunction.apply(e);
		acknowledgement.run();
		if (!wasProcessedByErrorHandler) throw e;
	}

	public AtomicBoolean getStopFlag() {
		return stopFlag;
	}
}
