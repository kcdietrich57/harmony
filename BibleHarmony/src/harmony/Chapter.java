package harmony;

/** Represents one chapter */
class Chapter {
	int book;
	int chapnum;
	int numverses;

	public Chapter(Book book, int chapnum, int numverses) {
		this.book = book.id;
		this.chapnum = chapnum;
		this.numverses = numverses;
	}
}