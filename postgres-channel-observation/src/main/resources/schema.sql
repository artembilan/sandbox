CREATE FUNCTION INT_CHANNEL_MESSAGE_NOTIFY_FCT()
    RETURNS TRIGGER AS
'
BEGIN
    PERFORM
pg_notify(''int_channel_message_notify'', NEW.REGION || '' '' || NEW.GROUP_KEY);
RETURN NEW;
END;
'
LANGUAGE PLPGSQL;

CREATE TRIGGER INT_CHANNEL_MESSAGE_NOTIFY_TRG
    AFTER INSERT
    ON INT_CHANNEL_MESSAGE
    FOR EACH ROW
    EXECUTE PROCEDURE INT_CHANNEL_MESSAGE_NOTIFY_FCT();