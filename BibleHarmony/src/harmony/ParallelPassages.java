package harmony;

import java.util.ArrayList;
import java.util.List;

/** Collection of passages that are duplicated (or single passage if unique) */
class ParallelPassages {
	List<Passage> passages = new ArrayList<Passage>();

	/** Print the passage(s) in a user friendly format */
	public static void dumpPassages(List<ParallelPassages> pps) {
		for (ParallelPassages pp : pps) {
			if (pp.passages.size() > 1) {
				System.out.println();
				System.out.println("=== " + pp.passages.size() + " =======================================");
			}

			boolean firstline = true;
			for (Passage p : pp.passages) {
				if (!firstline) {
					System.out.println();
				} else {
					firstline = false;
				}

				System.out.print(p.toString());
			}

			if (pp.passages.size() > 1) {
				System.out.println();
				System.out.println("-----------------------------------------");
			}

			System.out.println();
		}
	}

	/** Get the member passage for a specific book if it exists (or null) */
	public Passage getPassageForBook(Book book) {
		for (Passage p : this.passages) {
			if (p.start.book == book) {
				return p;
			}
		}

		return null;
	}

	private int compareTo(ParallelPassages other) {
		for (int ii = 0; ii < Book.books.size(); ++ii) {
			Book b = Book.books.get(ii);

			Passage thisp = getPassageForBook(b);
			if (thisp == null) {
				continue;
			}

			Passage otherp = other.getPassageForBook(b);
			if (otherp == null) {
				continue;
			}

			return thisp.compareTo(otherp);
		}

		return 0;
	}

	public String toString() {
		return "\n" + this.passages.size() + " " + this.passages.toString();
	}
}