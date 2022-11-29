package harmony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class PassageSorter {

	static List<ParallelPassages> sortPassages(List<ParallelPassages> ps) {
		List<ParallelPassages> retps = new ArrayList<ParallelPassages>();

		final Book matthew = Book.getBook("mt");
		final Book mark = Book.getBook("mk");
		final Book luke = Book.getBook("lk");
		final Book john = Book.getBook("jn");

		sortByBook(ps, mark);
		insertByBook(retps, ps, mark);
		sortByBook(ps, matthew);
		insertByBook(retps, ps, matthew);
		sortByBook(ps, luke);
		insertByBook(retps, ps, luke);
		sortByBook(ps, john);
		insertByBook(retps, ps, john);

		return retps;
	}

	/** Sort parallel passages by member position in a given book */
	static void sortByBook(List<ParallelPassages> pp, final Book bk) {
		Collections.sort(pp, new Comparator<ParallelPassages>() {
			public int compare(ParallelPassages pp1, ParallelPassages pp2) {
				Passage p1 = pp1.getPassageForBook(bk);
				Passage p2 = pp2.getPassageForBook(bk);

				if (p1 == null) {
					return (p2 != null) ? -1 : 0;
				}

				if (p2 == null) {
					return 1;
				}

				return p1.compareTo(p2);
			}
		});
	}

	static void insertByBook(List<ParallelPassages> retps, List<ParallelPassages> ps, Book bk) {
		int srcpos = 0;

		while (srcpos < ps.size()) {
			ParallelPassages srcp = ps.get(srcpos);
			if (srcp.getPassageForBook(bk) == null) {
				++srcpos;
				continue;
			}

			int inspos = findInsertPos(retps, srcp, bk);
			retps.add(inspos, srcp);
			ps.remove(srcpos);
		}
	}

	static int findInsertPos(List<ParallelPassages> pps, ParallelPassages p, Book bk) {
		int searchpos = 0;

		while (searchpos < pps.size()) {
			Passage pp = pps.get(searchpos).getPassageForBook(bk);

			if (pp != null && pp.compareTo(p.getPassageForBook(bk)) > 0) {
				break;
			}

			++searchpos;
		}

		return searchpos;
	}
}