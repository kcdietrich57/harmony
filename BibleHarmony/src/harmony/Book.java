package harmony;

import java.util.ArrayList;
import java.util.List;

/** Represents one book */
class Book {
	
	/** Metadata for all books */
	public static List<Book> books = new ArrayList<Book>();

	/** Initialize book metadatga */
	static {
		new Book("Matthew", "mt", "200Matthew.txt",
				new int[] { //
						25, 23, 17, 25, 48, 34, 29, 34, 38, 42, //
						30, 50, 58, 36, 39, 28, 27, 35, 30, 34, //
						46, 46, 39, 51, 46, 75, 66, 20 });

		new Book("Mark", "mk", "300Mark.txt",
				new int[] { //
						45, 28, 35, 41, 43, 56, 37, 38, 50, 52, //
						33, 44, 37, 72, 47, 20 });

		new Book("Luke", "lk", "400Luke.txt",
				new int[] { //
						80, 52, 38, 44, 39, 49, 50, 56, 62, 42, //
						54, 59, 35, 35, 32, 31, 37, 43, 48, 47, //
						38, 71, 56, 53 });

		new Book("John", "jn", "500John.txt",
				new int[] { //
						51, 25, 36, 54, 47, 71, 53, 59, 41, 42, //
						57, 50, 38, 31, 27, 33, 26, 40, 42, 31, //
						25 });
	}

	public static void checkFiles(BibleReader br) {
		for (Book b : books) {
			b.checkFile(br);
		}
	}

	/** Look up a book by name */
	public static Book getBook(String bkname) {
		for (Book b : books) {
			if (b.shortname.equalsIgnoreCase(bkname)) {
				return b;
			}
		}

		return null;
	}

	int id; // ID is increasing by book order, index in book array
	String name;
	String shortname;
	int numChapters; // TODO redundant
	List<Chapter> chapters;
	String filename;

	/** Construct book with metadata */
	public Book(String name, String shortname, String filename, int[] chapterLengths) {
		this.id = books.size();
		this.name = name;
		this.shortname = shortname;
		this.numChapters = chapterLengths.length;
		this.chapters = new ArrayList<Chapter>(this.numChapters);

		this.filename = filename;

		books.add(this);
		setChapterMetadata(chapterLengths);
	}

	public String toString() {
		return "Book[" + this.name + ", " + this.chapters.size() + "]";
	}

	/** Get a chapter by number */
	public Chapter chapter(int chapnum) {
		return (chapnum > 0 && chapnum <= this.chapters.size()) ? this.chapters.get(chapnum - 1) : null;
	}

	/** Create chapter metadata objects */
	public void setChapterMetadata(int[] lengths) {
		for (int len : lengths) {
			Chapter ch = new Chapter(this, this.chapters.size() + 1, len);
			this.chapters.add(ch);
		}
	}

	/** TODO Parse source file(s) */
	public void checkFile(BibleReader br) {
		br.parsePassages();
	}
}