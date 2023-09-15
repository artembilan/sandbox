# How to propagate security context over Spring Integration channels

This sample demonstrate a simple method security which is called from a scheduled thread via Spring Integration `PollingConsumer` with a message polled from `QueueChannel`.
Before the message is sent we authorise principal.
The `SecurityContextPropagationChannelInterceptor` added to the `myChannel` bean, propagates an `Authentication` from the current `SecurityContext` in the `preSend()` method via message header.
When this channel is consumed in the scheduled thread, that header is extracted and set into `SecurityContext` of that thread.
Without this interceptor the application fails with an `AuthenticationCredentialsNotFoundException`.

