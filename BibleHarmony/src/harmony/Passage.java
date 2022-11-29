package harmony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A passage is a contiguous text from a book, including parts of one or more
 * chapters/verses. May include descriptive metadata.
 */
class Passage {

	public static void coalesce(List<Passage> passages) {
		Collections.sort(passages, new Comparator<Passage>() {
			public int compare(Passage p1, Passage p2) {
				return p1.compareTo(p2);
			}
		});

		int anchor = 0;

		while (anchor < passages.size() - 1) {
			Passage p1 = passages.get(anchor);
			Passage p2 = passages.get(anchor + 1);

			if (p1.contains(p2)) {
				p1.addSubpassage(p2);

				passages.remove(p2);
				continue;
			}

			if (p2.contains(p1)) {
				p2.addSubpassage(p1);

				passages.remove(p1);
				continue;
			}

			boolean join = p1.abuts(p2);

			if (!join) {
				join = p1.abuts2(p2);
			}

			if (join) {
				Passage newp;

				if (p1.linenum == 0) {
					newp = p1;
					newp.end = p2.end;
				} else {
					newp = new Passage(p1.start, p2.end);
					newp.addSubpassage(p1);

					passages.set(anchor, newp);
				}

				newp.addSubpassage(p2);

				passages.remove(p2);
				continue;
			}

			if (p2.start.verse > 1) {
				p1.abuts(p2);
			}

			++anchor;
		}
	}

	String description;
	List<String> keywords;
	String text;
	Verse start;
	Verse end;
	int count; // TODO Verse count (incomplete, not used?)

	int filenum;
	int linenum;

	Passage parallel;
	List<Passage> subpassages;

	/** Construct simple passage from begin/end verse */
	public Passage(Verse start, Verse end) {
		this(start, end, "merged", null, 0, 0);
	}

	/** Construct passage with metadata */
	public Passage(Verse start, Verse end, String description, List<String> keywords, int filenum, int linenum) {
		this.description = description;
		this.start = start;
		this.end = end;

		if (start.compareTo(end) > 0) {
			System.out.println("Passage start is after end: " + start + " - " + end);

			this.start = end;
			this.end = start;
		}

		this.filenum = filenum;
		this.linenum = linenum;

		if (start.chapter == end.chapter) {
			this.count = end.verse - start.verse + 1;
		}

		this.subpassages = new ArrayList<Passage>();
		this.keywords = new ArrayList<String>();
		if (keywords != null) {
			this.keywords.addAll(keywords);
		}
	}

	/** Subpassage is a portion of a larger passage */
	public void addSubpassage(Passage sub) {
		if (!this.contains(sub)) {
			System.out.println("Passage " + this + " does not contain subpassage " + sub);
			return;
		}

		this.subpassages.add(sub);
	}

	/** Format the passage in a user-friendly way */
	public String toString() {
		String v = "---";

		v += this.start.toString();

		if (this.start.compareTo(this.end) == 0) {
			// do nothing
		} else if (this.start.book != this.end.book) {
			v += "-" + this.end.toString();
		} else if (this.start.chapter != this.end.chapter) {
			v += "-" + this.end.chapter + "." + this.end.verse;
		} else {
			v += "-" + this.end.verse;
		}

		v += " " + this.description + " (";

		boolean first = true;
		for (String kw : this.keywords) {
			if (!first) {
				v += ",";
			} else {
				first = false;
			}

			v += kw;
		}

		v += ")\n";

		return v + this.text;
	}

	/** Compare this passage to another according to their position in the text */
	public int compareTo(Passage other) {
		if (this.start.compareTo(other.start) != 0) {
			return this.start.compareTo(other.start);
		}

		return this.end.compareTo(other.end);
	}

	/** Return whether a given verse is contained in this passage */
	public boolean contains(Verse verse) {
		return this.start.compareTo(verse) <= 0 && this.end.compareTo(verse) >= 0;
	}

	/** Return whether a given passage is completely contained in this passage */
	public boolean contains(Passage passage) {
		return this.start.compareTo(passage.start) <= 0 && this.end.compareTo(passage.end) >= 0;
	}

	/** Return whether a given passage has verses in common with this passage */
	public boolean overlaps(Passage passage) {
		return !(this.start.compareTo(passage.end) > 0 || this.end.compareTo(passage.start) < 0);
	}

	/** Return whether a given passage is adjacent(following) this passage */
	public boolean abuts(Passage passage) {
		return abuts(passage, true);
	}

	/** Return whether a given passage is adjacent(following) this passage */
	public boolean abuts2(Passage passage) {
		return abuts(passage, false);
	}

	/**
	 * Return whether a given passage is adjacent(following) this passage<br>
	 * TODO abut()needs work?<br>
	 * 1. Must be in same book<br>
	 * 2. Must align to last verse of this passage
	 */
	public boolean abuts(Passage passage, boolean strict) {
		if (this.start.book != passage.start.book) {
			return false;
		}

		// Same chapter
		if (this.end.chapter == passage.start.chapter) {
			return this.end.verse + 1 == passage.start.verse //
					|| (!strict && this.end.verse == passage.start.verse);
		}

		// Next chapter
		return passage.start.verse == 1 //
				&& this.end.verse == this.end.book.chapter(this.end.chapter).numverses;
	}
}