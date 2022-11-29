package harmony;

/** Represents a single verse (or parital verse) */
class Verse {
	Book book;
	int chapter;
	int verse;
	int partial;

	/** Constructor for partial verse (a/b/c) */
	public Verse(Book book, int chap, int verse, int partial) {
		this.book = book;
		this.chapter = chap;
		this.verse = verse;
		this.partial = partial;
	}

	/** Constructor for complete verse */
	public Verse(Book book, int chap, int verse) {
		this.book = book;
		this.chapter = chap;
		this.verse = verse;
		this.partial = -1;
	}

	/** Output verse in user-friendly format */
	public String toString() {
		String ret = this.book.name + " " + this.chapter + "." + this.verse;

		if (this.partial >= 0) {
			ret += (char) ('a' + partial);
		}

		return ret;
	}

	/** Compare to another verse as regards position in the text */
	public int compareTo(Verse other) {
		if (this.book != other.book) {
			return this.book.id - other.book.id;
		}

		if (this.chapter != other.chapter) {
			return this.chapter - other.chapter;
		}

		if (this.verse != other.verse) {
			return this.verse - other.verse;
		}

		return this.partial - other.partial;
	}
}