package harmony;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** Ingests bible data file(s) */
class BibleReader {

	public static void main(String[] args) {
		BibleReader rdr = new BibleReader("test.txt");
		List<ParallelPassages> passages = rdr.parsePassages();
		//System.out.println(passages.toString());

		ParallelPassages.dumpPassages(PassageSorter.sortPassages(passages));
	}

	/** File containing data */
	String filename;

	/** Constructor */
	public BibleReader(String filename) {
		this.filename = filename;
	}

	/** Parse passage data into internal structures */
	public void readPassages(List<ParallelPassages> passages) {
		List<ParallelPassages> ps = parsePassages();
		passages.addAll(ps);
	}

	/** Ingest data from file and create parallel passages metadata */
	public List<ParallelPassages> parsePassages() {
		int filenum = 1;

		File f = new File(this.filename);
		if (!f.exists() || !f.canRead()) {
			System.out.println("Can't read file " + this.filename);
			return null;
		}

		List<ParallelPassages> passages = new ArrayList<ParallelPassages>();

		FileReader fr = null;
		LineNumberReader lr = null;

		boolean collectingParallel = false;
		ParallelPassages currParallel = null;
		Passage currPassage = null;

		try {
			fr = new FileReader(f);
			lr = new LineNumberReader(fr);

			boolean inVerse = false;

			// Input syntax:
			// ===
			// ----
			// ---
			// }
			// [digit]

			for (;;) {
				String line = lr.readLine();
				if (line == null) {
					break;
				}

				if (line.startsWith("#")) {
					continue;
				}

				if (line.startsWith("===")) {
					inVerse = false;
					collectingParallel = true;
					currParallel = new ParallelPassages();
					passages.add(currParallel);
					continue;
				}

				if (line.startsWith("----")) {
					inVerse = false;
					collectingParallel = false;
					continue;
				}

				if (line.startsWith("---")) {
					if (inVerse && currPassage != null) {
						currPassage.text += "\n";
					}
					inVerse = false;
					currPassage = makePassage(line, filenum, lr.getLineNumber());
					if (currPassage == null) {
						continue;
					}

					if (!collectingParallel) {
						currParallel = new ParallelPassages();
						passages.add(currParallel);
					}

					currParallel.passages.add(currPassage);

					continue;
				}

				if (line.startsWith("}")) {
					continue;
				}

				if (currPassage != null) {
					String tl = line.trim();
					if (tl.length() == 0) {
						continue;
					}

					if (Character.isDigit(tl.charAt(0))) {
						int textoff = 0;
						while (textoff < tl.length() //
								&& Character.isDigit(tl.charAt(textoff))) {
							++textoff;
						}

						int verseNum = Integer.parseInt(tl.substring(0, textoff));
						String verseText = tl.substring(textoff).trim();

						if (currPassage.text == null) {
							currPassage.text = "";
						}
						if (inVerse) {
							currPassage.text += "\n";
						}
						inVerse = true;

						currPassage.text += "" + verseNum + " " + verseText;
					} else {
						currPassage.text += " " + tl;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("[" + lr.getLineNumber() + "] error");
			e.printStackTrace();
		} finally {
			try {
				if (lr != null)
					lr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return passages;
	}

	private Passage makePassage(String line, int filenum, int linenum) {
		// Syntax
		// ---REF { DESC
		// REF: BBBc.v[-c.v]
		// DESC: ...(KEYWORDS)

		Book bk = null;
		int chap = 0;
		int verse = 0;
		int chap2 = 0;
		int verse2 = 0;
		String desc = null;

		int brace = line.indexOf('{');
		if (brace <= 0) {
			System.out.println("[" + linenum + "] Syntax error: " + line);
			return null;
		}

		desc = line.substring(brace + 1).trim();
		line = line.substring(0, brace).trim();

		List<String> keywords = new ArrayList<String>();

		int paren = desc.indexOf('(');
		if (paren >= 0) {
			int rparen = desc.indexOf(')', paren + 1);

			if (rparen >= 0) {
				String kwstring = desc.substring(paren + 1, rparen).trim();
				desc = desc.substring(0, paren).trim();

				parseKeywords(kwstring, keywords);
			}
		}

		if (line.length() < 5) {
			System.out.println("[" + linenum + "] Syntax error: " + line);
			return null;
		}

		String bkname = line.substring(3, 5).trim();
		line = line.substring(5).trim();

		bk = Book.getBook(bkname);
		if (bk == null) {
			System.out.println("Can't find book " + bkname);
			return null;
		}

		int dot = line.indexOf('.');
		if (dot <= 0 || dot >= line.length()) {
			System.out.println("[" + linenum + "] Syntax error: " + line);
			return null;
		}

		String schap = line.substring(0, dot).trim();
		line = line.substring(dot + 1).trim();

		chap = Integer.parseInt(schap);
		if (chap < 1 || chap > bk.numChapters) {
			System.out.println("[" + linenum + "] Invalid chapter: " + chap);
		}

		String sverse = line;

		int dash = line.indexOf('-');
		if (dash >= 0) {
			if (dash == 0 || dot >= line.length()) {
				System.out.println("[" + linenum + "] Syntax error: " + line);
				return null;
			}

			sverse = line.substring(0, dash).trim();
			line = line.substring(dash + 1).trim();
		}

		int partial = -1;
		if (sverse.endsWith("a") || sverse.endsWith("b") || sverse.endsWith("c")) {
			int len = sverse.length();
			char ew = sverse.charAt(len - 1);

			sverse = sverse.substring(0, len - 1);

			partial = ew - 'a';
		}

		verse = Integer.parseInt(sverse);
		if (verse < 0 || verse > bk.chapter(chap).numverses) {
			System.out.println("[" + linenum + "] invalid verse: " + verse);
			return null;
		}

		dot = line.indexOf('.');
		if (dot > 0) {
			String schap2 = line.substring(0, dot);
			line = line.substring(dot + 1);

			chap2 = Integer.parseInt(schap2);
		} else {
			chap2 = chap;
		}

		String sverse2 = line;

		int partial2 = -1;
		if (sverse2.endsWith("a") || sverse2.endsWith("b") || sverse2.endsWith("c")) {
			int len = sverse2.length();
			char ew = sverse2.charAt(len - 1);

			sverse2 = sverse2.substring(0, len - 1);

			partial2 = ew - 'a';
		}

		verse2 = Integer.parseInt(sverse2);
		if (chap2 < chap || (chap2 == chap && (verse2 < verse || verse2 > bk.chapter(chap).numverses))) {
			System.out.println("[" + linenum + "] invalid verse: " + verse2);
			return null;
		}

		Verse v1 = new Verse(bk, chap, verse, partial);
		Verse v2 = new Verse(bk, chap2, verse2, partial2);

		return new Passage(v1, v2, desc, keywords, filenum, linenum);
	}

	private void parseKeywords(String line, List<String> keywords) {
		StringTokenizer toker = new StringTokenizer(line, ",");

		while (toker.hasMoreTokens()) {
			String tok = toker.nextToken();
			keywords.add(tok);
		}
	}

	private List<ParallelPassages> parsePassagesV2(String filename, int filenum) {
		System.out.println("Checking file for " + filename);

		File f = new File(new File("/education/bible"), filename);
		if (!f.exists() || !f.canRead()) {
			System.out.println("Can't read file " + filename);
			return null;
		}

		List<ParallelPassages> passages = new ArrayList<ParallelPassages>();

		FileReader fr = null;
		LineNumberReader lr = null;
		boolean collectingParallel = false;
		ParallelPassages currParallel = null;
		Passage currPassage = null;

		try {
			fr = new FileReader(f);
			lr = new LineNumberReader(fr);

			for (;;) {
				String line = lr.readLine();
				if (line == null) {
					break;
				}

				if (line.startsWith("===")) {
					collectingParallel = true;
					currParallel = new ParallelPassages();
					passages.add(currParallel);
					continue;
				}
				if (line.startsWith("----")) {
					collectingParallel = false;
					continue;
				}

				if (line.startsWith("---")) {
					currPassage = makePassage(line, filenum, lr.getLineNumber());
					if (currPassage == null) {
						continue;
					}

					if (!collectingParallel) {
						currParallel = new ParallelPassages();
						passages.add(currParallel);
					}

					currParallel.passages.add(currPassage);

					continue;
				}

				if (currPassage != null) {
					String tl = line.trim();

					if (tl.length() > 0 && Character.isDigit(tl.charAt(0))) {
						int textoff = line.indexOf(' ');
						int verseNum = Integer.parseInt(line.substring(0, textoff));
						String verseText = line.substring(textoff + 1);

						if (currPassage.text == null) {
							currPassage.text = "";
						}
						currPassage.text += "" + verseNum + " " + verseText + "\n";
					}
				}
			}
		} catch (Exception e) {
			System.out.println("[" + lr.getLineNumber() + "] error");
			e.printStackTrace();
		} finally {
			try {
				if (lr != null)
					lr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return passages;
	}

	private Passage makePassageV2(String line, int filenum, int linenum) {
		Book bk = null;
		int chap = 0;
		int verse = 0;
		int chap2 = 0;
		int verse2 = 0;
		String desc = null;

		int brace = line.indexOf('{');
		if (brace <= 0) {
			System.out.println("[" + linenum + "] Syntax error: " + line);
			return null;
		}

		desc = line.substring(brace + 1).trim();
		line = line.substring(0, brace).trim();

		if (line.length() < 5) {
			System.out.println("[" + linenum + "] Syntax error: " + line);
			return null;
		}

		String bkname = line.substring(3, 5).trim();
		line = line.substring(5).trim();

		bk = Book.getBook(bkname);
		if (bk == null) {
			System.out.println("Can't find book " + bkname);
			return null;
		}

		int dot = line.indexOf('.');
		if (dot <= 0 || dot >= line.length()) {
			System.out.println("[" + linenum + "] Syntax error: " + line);
			return null;
		}

		String schap = line.substring(0, dot).trim();
		line = line.substring(dot + 1).trim();

		chap = Integer.parseInt(schap);
		if (chap < 1 || chap > bk.numChapters) {
			System.out.println("[" + linenum + "] Invalid chapter: " + chap);
		}

		String sverse = line;

		int dash = line.indexOf('-');
		if (dash >= 0) {
			if (dash == 0 || dot >= line.length()) {
				System.out.println("[" + linenum + "] Syntax error: " + line);
				return null;
			}

			sverse = line.substring(0, dash).trim();
			line = line.substring(dash + 1).trim();
		}

		verse = Integer.parseInt(sverse);
		if (verse < 0 || verse > bk.chapter(chap).numverses) {
			System.out.println("[" + linenum + "] invalid verse: " + verse);
			return null;
		}

		dot = line.indexOf('.');
		if (dot > 0) {
			String schap2 = line.substring(0, dot);
			line = line.substring(dot + 1);

			chap2 = Integer.parseInt(schap2);
		} else {
			chap2 = chap;
		}

		String sverse2 = line;

		verse2 = Integer.parseInt(sverse2);
		if (chap2 < chap || (chap2 == chap && (verse2 < verse || verse2 > bk.chapter(chap).numverses))) {
			System.out.println("[" + linenum + "] invalid verse: " + verse2);
			return null;
		}

		Verse v1 = new Verse(bk, chap, verse);
		Verse v2 = new Verse(bk, chap2, verse2);
		return new Passage(v1, v2, desc, null, filenum, linenum);
	}
}