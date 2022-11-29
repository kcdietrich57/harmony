package harmony;

import java.util.ArrayList;
import java.util.List;

// Some comment
public class Harmony {
	public static void main(String[] args) {
		BibleReader br = new BibleReader("/tmp/harmony.txt");

		Book.checkFiles(br);

		List<ParallelPassages> ps = new ArrayList<ParallelPassages>();
		br.readPassages(ps);

		// Passage.coalesce(ps);

		List<ParallelPassages> sortedPPS = PassageSorter.sortPassages(ps);

		ParallelPassages.dumpPassages(sortedPPS);
	}

	// public static List<ParallelPassages> parsePassages(int filenum) {
	// return parsePassages(files[filenum], 0);
	// }
}