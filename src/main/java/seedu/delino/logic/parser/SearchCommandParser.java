package seedu.delino.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.delino.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_COD;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_COMMENT;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_DELIVERY_TIMESTAMP;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_RETURN_TIMESTAMP;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_TID;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_TYPE;
import static seedu.delino.logic.parser.CliSyntax.PREFIX_WAREHOUSE;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.delino.commons.core.LogsCenter;
import seedu.delino.logic.commands.SearchCommand;
import seedu.delino.logic.parser.exceptions.ParseException;
import seedu.delino.model.parcel.OrderContainsKeywordsPredicate;
import seedu.delino.model.parcel.ReturnOrderContainsKeywordsPredicate;

//@@author khsc96
/**
 * Parses input arguments and creates a new SearchCommand object
 */
public class SearchCommandParser implements Parser<SearchCommand> {

    private static final Logger LOGGER = LogsCenter.getLogger(SearchCommandParser.class.getName());

    /**
     * Parses the given {@code String} of arguments in the context of the SearchCommand
     * and returns a SearchCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public SearchCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Flag flag = null;

        if (areFlagsPresent(args)) {
            flag = extractFlag(args);
            args = removeFlags(args);
        }

        ArgumentMultimap argMultimap =
            ArgumentTokenizer.tokenize(args, PREFIX_TID, PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_COD,
                PREFIX_DELIVERY_TIMESTAMP, PREFIX_RETURN_TIMESTAMP, PREFIX_WAREHOUSE, PREFIX_COMMENT, PREFIX_TYPE,
                PREFIX_EMAIL);

        boolean prefixesPresent = arePrefixesPresent(argMultimap, PREFIX_TID, PREFIX_NAME, PREFIX_PHONE,
            PREFIX_ADDRESS, PREFIX_COD, PREFIX_DELIVERY_TIMESTAMP, PREFIX_EMAIL, PREFIX_RETURN_TIMESTAMP,
            PREFIX_WAREHOUSE, PREFIX_COMMENT, PREFIX_TYPE);

        if (!prefixesPresent && argMultimap.getPreamble().isEmpty()) {
            LOGGER.info("Invalid user input, parse failure: No input given other than search keyword.");
            throw new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCommand.MESSAGE_USAGE));
        }

        List<String> keywords =
            Arrays.asList(addPrefixKeywordsToList(argMultimap).toString().trim().split("\\s"));
        keywords = processKeywords(keywords);

        if (flag == null) {
            LOGGER.fine("No flag present, SearchCommand object returned with no flag given.");
            return prefixesPresent
                ? new SearchCommand(new OrderContainsKeywordsPredicate(keywords, argMultimap),
                new ReturnOrderContainsKeywordsPredicate(keywords, argMultimap))
                : new SearchCommand(new OrderContainsKeywordsPredicate(keywords),
                new ReturnOrderContainsKeywordsPredicate(keywords));
        }

        if (flag.equals(CliSyntax.FLAG_ORDER_BOOK)) {
            LOGGER.fine("Order flag present, SearchCommand object returned with Order flag given.");
            return prefixesPresent
                ? new SearchCommand(new OrderContainsKeywordsPredicate(keywords, argMultimap))
                : new SearchCommand(new OrderContainsKeywordsPredicate(keywords));
        } else if (flag.equals(CliSyntax.FLAG_RETURN_BOOK)) {
            LOGGER.fine("Return flag present, SearchCommand object returned with Return flag given.");
            return prefixesPresent
                ? new SearchCommand(new ReturnOrderContainsKeywordsPredicate(keywords, argMultimap))
                : new SearchCommand(new ReturnOrderContainsKeywordsPredicate(keywords));
        }

        LOGGER.info("Invalid input, parse failure: Flag given is not valid.");
        throw new ParseException(String.format(
            MESSAGE_INVALID_COMMAND_FORMAT, SearchCommand.MESSAGE_USAGE));
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).anyMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

    /**
     * Returns a StringBuilder object of all the values user keyed in.
     *
     * @param argumentMultimap An {@code ArgumentMultimap} object containing all the keywords tagged to a
     *                         specific prefix
     * @return returns a StringBuilder object of all the values user keyed in with a trailing whitespace behind.
     */
    private static StringBuilder addPrefixKeywordsToList(ArgumentMultimap argumentMultimap) {
        StringBuilder keywords = new StringBuilder();
        List<String> currentPrefixListOfKeywords = argumentMultimap.getAllPrefixValues();
        currentPrefixListOfKeywords.stream().map(each -> each + " ").forEach(keywords::append);
        LOGGER.finest(String.format("Values of all keywords user gave: %s", keywords.toString()));
        return keywords;
    }

    /**
     * Checks for presence of any flag given by user, return true if there is any.
     * @param string String object representing the user input.
     * @return Return boolean value representing the presence of any flags.
     */
    private boolean areFlagsPresent(String string) {
        List<String> listOfStr = Arrays.asList(string.split("\\s"));
        return Stream
            .of(CliSyntax.FLAG_RETURN_BOOK, CliSyntax.FLAG_ORDER_BOOK)
            .anyMatch(flag -> listOfStr.contains(flag.getFlag()));
    }

    /**
     * Returns a {@code Flag} object according to the flag user provided in.
     * @param arg String object representing the user input.
     * @return {@code Flag} object representing the flag given.
     * @throws ParseException ParseException is thrown when multiple different flags are detected.
     */
    private Flag extractFlag(String arg) throws ParseException {
        List<String> argArr = Arrays.asList(arg.trim().split("\\s"));
        if (argArr.contains(CliSyntax.FLAG_ORDER_BOOK.getFlag())
            && argArr.contains(CliSyntax.FLAG_RETURN_BOOK.getFlag())) {
            LOGGER.info("Invalid input, parse failure: Multiple flags given.");
            throw new ParseException(SearchCommand.MULTIPLE_FLAGS_DETECTED);
        }

        if (argArr.contains(CliSyntax.FLAG_RETURN_BOOK.getFlag())) {
            LOGGER.finest("Return flag found.");
            return CliSyntax.FLAG_RETURN_BOOK;
        } else {
            LOGGER.finest("Order flag found.");
            return CliSyntax.FLAG_ORDER_BOOK;
        }
    }

    /**
     * Returns a String object with all flags removed from it.
     * @param args String object representing the input given by the user.
     * @return a String object with all flags removed from it.
     */
    private String removeFlags(String args) {
        List<String> listOfArgs = Arrays.asList(args.split("\\s"));
        String returnString = listOfArgs
            .stream()
            .filter(each -> Stream
                .of(CliSyntax.FLAG_RETURN_BOOK, CliSyntax.FLAG_ORDER_BOOK)
                .noneMatch(flag -> each.equals(flag.getFlag())))
            .map(each -> each + " ")
            .collect(Collectors.joining());
        LOGGER.finest(String.format("User input after removing flags %s", returnString));
        return returnString;
    }

    /**
     * Given a {@code List<String>} object of all the keywords user want to search for,
     * ensure all element in the list of string is a word.
     * @param keywords A {@code List<String>} of keywords given by the user.
     * @return {@code List<String} object with one word as each element.
     */
    private List<String> processKeywords(List<String> keywords) {
        List<String> returnList = keywords.stream().filter(each -> !each.isEmpty()).collect(Collectors.toList());
        return returnList;
    }
}
