package seedu.delino.logic.parser;

import static seedu.delino.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_COMMENT;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_RETURN_TIMESTAMP;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_TID;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_TYPE;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_WAREHOUSE;

import java.util.logging.Logger;
import java.util.stream.Stream;

import seedu.delino.commons.core.LogsCenter;
import seedu.delino.logic.commands.ReturnCommand;
import seedu.delino.logic.parser.exceptions.ParseException;
import seedu.delino.model.parcel.optionalparcelattributes.Comment;
import seedu.delino.model.parcel.optionalparcelattributes.TypeOfItem;
import seedu.delino.model.parcel.parcelattributes.Address;
import seedu.delino.model.parcel.parcelattributes.Email;
import seedu.delino.model.parcel.parcelattributes.Name;
import seedu.delino.model.parcel.parcelattributes.Phone;
import seedu.delino.model.parcel.parcelattributes.TimeStamp;
import seedu.delino.model.parcel.parcelattributes.TransactionId;
import seedu.delino.model.parcel.parcelattributes.Warehouse;
import seedu.delino.model.parcel.returnorder.ReturnOrder;

//@@author cherweijie
/**
 * Parses input arguments and creates a new AddCommand object
 */
public class ReturnCommandParser implements Parser<ReturnCommand> {
    private static final Logger logger = LogsCenter.getLogger(ReturnCommandParser.class);
    /**
     * Parses the given {@code String} of arguments in the context of the ReturnCommand
     * and returns an ReturnCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public ReturnCommand parse(String args) throws ParseException {
        logger.fine("parsing of parcel attributes.");
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_TID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_RETURN_TIMESTAMP, PREFIX_WAREHOUSE, PREFIX_TYPE,
                        PREFIX_COMMENT);

        if (onlyTidAndReturnTimeStampPresent(argMultimap)) {
            logger.fine("checking if only Transaction ID and return time stamp"
                    + "are present.");
            TransactionId tid = ParserUtil.parseTid(argMultimap.getValue(PREFIX_TID).get());
            TimeStamp timeStamp = ParserUtil.parseTimeStamp(argMultimap.getValue(PREFIX_RETURN_TIMESTAMP).get());
            return new ReturnCommand(null, tid, timeStamp);
        }

        if (anyCompulsoryPrefixMissing(argMultimap)) {
            logger.info(" compulsory prefixes are missing from user input.");
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReturnCommand.MESSAGE_USAGE));
        }
        ReturnOrder returnOrder = createReturnOrder(argMultimap);
        TransactionId tid = returnOrder.getTid();
        TimeStamp timeStamp = returnOrder.getTimestamp();
        return new ReturnCommand(returnOrder, tid, timeStamp);
    }

    /**
     * Checks if only the transaction id is present to trigger a different logic for the return command.
     * {@param argMultimap}
     */
    private boolean onlyTidAndReturnTimeStampPresent(ArgumentMultimap argMultimap) {
        logger.fine("Within the onlyTidAndReturnTimeStampPresent method.");
        return arePrefixesPresent(argMultimap, PREFIX_TID)
                && arePrefixesPresent(argMultimap, PREFIX_RETURN_TIMESTAMP)
                && !arePrefixesPresent(argMultimap, PREFIX_NAME)
                && !arePrefixesPresent(argMultimap, PREFIX_ADDRESS)
                && !arePrefixesPresent(argMultimap, PREFIX_PHONE)
                && !arePrefixesPresent(argMultimap, PREFIX_WAREHOUSE)
                && !arePrefixesPresent(argMultimap, PREFIX_EMAIL);
    }

    /**
     * Check if any compulsory prefixes are missing in the user input.
     * @param argMultimap The arguments parsed by user input.
     * @return true if any compulsory prefixes are missing.
     */
    private boolean anyCompulsoryPrefixMissing(ArgumentMultimap argMultimap) {
        logger.fine("Within the anyCompulsoryPrefixMissing method.");
        return !arePrefixesPresent(argMultimap, PREFIX_TID, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_RETURN_TIMESTAMP, PREFIX_WAREHOUSE)
                || !argMultimap.getPreamble().isEmpty();
    }

    /**
     * Creates a return order based on the input keyed in by the user.
     * @param argMultimap The arguments parsed by user input.
     * @return ReturnOrder A return order based on attributes keyed in by user.
     * @throws ParseException An exception will be thrown if input is invalid.
     */
    private ReturnOrder createReturnOrder(ArgumentMultimap argMultimap) throws ParseException {
        logger.fine("Creating a new return order based on user input");
        TransactionId tid = ParserUtil.parseTid(argMultimap.getValue(PREFIX_TID).get());
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        TimeStamp timeStamp = ParserUtil.parseTimeStamp(argMultimap.getValue(PREFIX_RETURN_TIMESTAMP).get());
        Warehouse warehouse = ParserUtil.parseWarehouse(argMultimap.getValue(PREFIX_WAREHOUSE).get());
        Comment comment = ParserUtil.parseComment(argMultimap.getValue(PREFIX_COMMENT).isEmpty()
                ? "NIL"
                : argMultimap.getValue(PREFIX_COMMENT).get());
        TypeOfItem type = ParserUtil.parseItemType(argMultimap.getValue(PREFIX_TYPE).isEmpty()
                ? "NIL"
                : argMultimap.getValue(PREFIX_TYPE).get());

        ReturnOrder returnOrder = new ReturnOrder(tid, name, phone, email, address,
                timeStamp, warehouse, comment, type);
        return returnOrder;
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        logger.fine("Check if prefixes are present");
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
