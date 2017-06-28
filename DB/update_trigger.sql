drop trigger if exists on_new_messages;
DELIMITER //
create trigger on_new_messages
after insert on Message for each row
begin

declare currUpdateFlag int;
declare currUserId int;

set currUserId = (select User_id from Message where id = NEW.id);
-- set currUpdateFlag = (select update_status from User where id = currUserId);
update User set update_status = 1 where id = currUserId;
end;
//
DELIMITER ;