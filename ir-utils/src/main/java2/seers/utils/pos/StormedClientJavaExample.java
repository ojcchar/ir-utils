package seers.utils.pos;

public class StormedClientJavaExample {

	// public static void main(final String args[]) {
	// final String codeToParse = "Lorem ipsum dolor sit amet, consectetur
	// adipiscing elit\n"
	// + "public static void main(int args[])\n"
	// + "Proin tincidunt tristique ante, sed lacinia leo fermentum quis.\n"
	// + "Fusce in magna eu ante tincidunt euismod nec eu ligula.\n" + "List
	// someList;";
	//
	// final String key = "<your API key>";
	// final Response response = StormedService.parse(codeToParse, key);
	//
	// /*
	// * Whenever accessing fields of Scala case classes, getters are always
	// * in the form variableName.fieldName();
	// */
	// switch (response.status()) {
	// case "OK":
	// final SuccessResponse success = (SuccessResponse) response;
	// System.out.println("Status: " + success.status());
	// System.out.println("Quota Remaining: " + success.quotaRemaining());
	// System.out.println("Parsing Result: ");
	// printHASTNodes(success.result());
	// break;
	// case "ERROR":
	// final ErrorResponse error = (ErrorResponse) response;
	// System.out.println(error.status() + ": " + error.message());
	// break;
	// }
	//
	// }
	//
	// /*
	// * All HASTNode carrying a Scala collection can be easily converted to a
	// * Java collection by using Scala' SDK conversion for java. For matter of
	// * convenience, just statically import asJavaCollection as above and use
	// it
	// * to convert Seq to Collection as below.
	// */
	// private static void printHASTNodes(final Seq result) {
	// final Collection collection = asJavaCollection(result);
	// for (final HASTNode node : collection) {
	// System.out.println(node.getClass().getSimpleName());
	// }
	// }
}
