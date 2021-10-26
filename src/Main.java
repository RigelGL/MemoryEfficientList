import com.sun.security.jgss.GSSUtil;
import rigellab.MemoryEfficientList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    private static void testPerformanceIntegerToArray(int n) throws Exception {
        List<Integer> mel = new MemoryEfficientList(Integer.class);
        List<Integer> al = new ArrayList<>();

        long minMel = Long.MAX_VALUE;
        long minAr = Long.MAX_VALUE;
        long totalMel = 0;
        long totalAr = 0;
        int ignored = 5;
        int count = 10;

        for(int h = 0; h < ignored + count; h++) {
            Random r = new Random(234345465556425653L);

            mel.clear();
            al.clear();

            for(int i = 0; i < n; i++) {
                int k = r.nextInt();
                mel.add(k);
                al.add(k);
            }

            long t = System.nanoTime();
            Object[] a = mel.toArray();
            long deltaMel = System.nanoTime() - t;

            t = System.nanoTime();
            Object[] b = al.toArray();
            long deltaAr = System.nanoTime() - t;

            if(h >= ignored) {
                minMel = Math.min(minMel, deltaMel);
                minAr = Math.min(minAr, deltaAr);
                totalMel += deltaMel;
                totalAr += deltaAr;
            }
        }

        totalMel /= count;
        totalAr /= count;

        System.out.println("IntegerToArray: n=" + n + "\n" +
                "MEL: min=" + minMel * 1e-9f + ", avg=" + totalMel * 1e-9f + "\n" +
                "AL: min=" + minAr * 1e-9f + ", avg=" + totalAr * 1e-9f);

    }


    private static void testPerformanceIntegerRandomSwapping(int n) throws Exception {
        List<Long> mel = new MemoryEfficientList(Long.class);
        List<Long> al = new ArrayList<>();

        long minMel = Long.MAX_VALUE;
        long minAr = Long.MAX_VALUE;
        long totalMel = 0;
        long totalAr = 0;
        int ignored = 5;
        int count = 10;

        for(int h = 0; h < ignored + count; h++) {
            Random r = new Random(234345465556425653L);

            mel.clear();
            al.clear();

            for(int i = 0; i < n; i++) {
                long k = r.nextLong();
                mel.add(k);
                al.add(k);
            }

            r.setSeed(1979804576454564L);
            long t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                Long a = mel.get(pos1);
                Long b = mel.get(pos2);
                mel.set(pos1, b);
                mel.set(pos2, a);
            }
            long deltaMel = System.nanoTime() - t;

            r.setSeed(1979804576454564L);
            t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                Long a = al.get(pos1);
                Long b = al.get(pos2);
                al.set(pos1, b);
                al.set(pos2, a);
            }
            long deltaAr = System.nanoTime() - t;

            if(h >= ignored) {
                minMel = Math.min(minMel, deltaMel);
                minAr = Math.min(minAr, deltaAr);
                totalMel += deltaMel;
                totalAr += deltaAr;
            }

            if(mel.hashCode() != al.hashCode()) {
                System.err.println("INVALID HASHES");
            }
        }

        totalMel /= count;
        totalAr /= count;

        System.out.println("IntegerRandomSwapping: n=" + n + "\n" +
                "MEL: min=" + minMel * 1e-9f + ", avg=" + totalMel * 1e-9f + "\n" +
                "AL: min=" + minAr * 1e-9f + ", avg=" + totalAr * 1e-9f);

    }


    public static void main(String[] args) throws Exception {
//        testPerformanceIntegerToArray(10_000_000);
        testPerformanceIntegerRandomSwapping(10_000_000);

    }
}
