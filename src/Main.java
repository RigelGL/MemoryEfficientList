import com.sun.security.jgss.GSSUtil;
import rigellab.MemoryEfficientList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

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
        List<Integer> mel = new MemoryEfficientList(Integer.class, n);
        List<Integer> al = new ArrayList<>(n);

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

            r.setSeed(1979804576454564L);
            long t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                Integer a = (Integer) ((MemoryEfficientList) mel).getFast(pos1);
                Integer b = (Integer) ((MemoryEfficientList) mel).getFast2(pos2);
                mel.set(pos1, b);
                mel.set(pos2, a);
            }
            long deltaMel = System.nanoTime() - t;

            r.setSeed(1979804576454564L);
            t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                Integer a = al.get(pos1);
                Integer b = al.get(pos2);
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

        System.out.println("IntegerRandomSwapping (with getFast): n=" + n + "\n" +
                "MEL: min=" + minMel * 1e-9f + ", avg=" + totalMel * 1e-9f + "\n" +
                "AL: min=" + minAr * 1e-9f + ", avg=" + totalAr * 1e-9f);
    }


    private static void testPerformancePrimes(int n) throws Exception {
        List<Boolean> melIs = new MemoryEfficientList(Boolean.class, n);
        List<Integer> melResult = new MemoryEfficientList(Integer.class);

        List<Boolean> alIs = new ArrayList<>(n);
        List<Integer> alResult = new ArrayList<>();

        long minMel = Long.MAX_VALUE;
        long minAr = Long.MAX_VALUE;
        long totalMel = 0;
        long totalAr = 0;
        int ignored = 5;
        int count = 10;

        for(int h = 0; h < ignored + count; h++) {
            melIs.clear();
            melResult.clear();

            alIs.clear();
            alResult.clear();

            for(int i = 0; i < n; i++) {
                melIs.add(true);
                alIs.add(true);
            }

            melIs.set(0, false);
            melIs.set(1, false);

            alIs.set(0, false);
            alIs.set(1, false);

            long t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                if(((Boolean)((MemoryEfficientList) melIs).getFast(i)) && i * i > 0) {
                    for(int j = i * i; j < n; j += i)
                        melIs.set(j, false);
                    melResult.add(i);
                }
            }
            long deltaMel = System.nanoTime() - t;

            t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                if(alIs.get(i) && i * i > 0) {
                    for(int j = i * i; j < n; j += i)
                        alIs.set(j, false);
                    alResult.add(i);
                }
            }

            long deltaAr = System.nanoTime() - t;

            if(h >= ignored) {
                minMel = Math.min(minMel, deltaMel);
                minAr = Math.min(minAr, deltaAr);
                totalMel += deltaMel;
                totalAr += deltaAr;
            }

            if(alResult.size() != melResult.size()) {
                System.out.println("INVALID CHECK");
                return;
            }

            for(int i = 0; i < alResult.size(); i++) {
                if(alResult.get(i).intValue() != melResult.get(i).intValue()) {
                    System.out.println("INVALID CHECK");
                    return;
                }
            }
        }

        totalMel /= count;
        totalAr /= count;

        System.out.println("Primes (with getFast): n=" + n + "\n" +
                "MEL: min=" + minMel * 1e-9f + ", avg=" + totalMel * 1e-9f + "\n" +
                "AL: min=" + minAr * 1e-9f + ", avg=" + totalAr * 1e-9f);
    }


    private static void testPerformanceLargeObjectRandomSwapping(int n) throws Exception {
        List<LargeObject> mel = new MemoryEfficientList(LargeObject.class, n);
        List<LargeObject> al = new ArrayList<>(n);

        long minMel = Long.MAX_VALUE;
        long minAr = Long.MAX_VALUE;
        long totalMel = 0;
        long totalAr = 0;
        int ignored = 5;
        int count = 10;

        Random r = new Random();

        for(int h = 0; h < ignored + count; h++) {
            mel.clear();
            al.clear();

            for(int i = 0; i < n; i++) {
                LargeObject lo = new LargeObject();
                lo.init();
                mel.add(lo);
                al.add(lo);
            }

            r.setSeed(1979804576454564L);
            long t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                LargeObject a = (LargeObject) ((MemoryEfficientList) mel).getFast(pos1);
                LargeObject b = (LargeObject) ((MemoryEfficientList) mel).getFast2(pos2);
                mel.set(pos1, b);
                mel.set(pos2, a);
            }
            long deltaMel = System.nanoTime() - t;

            r.setSeed(1979804576454564L);
            t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                LargeObject a = al.get(pos1);
                LargeObject b = al.get(pos2);
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

        System.out.println("LargeObjectRandomSwapping (with getFast): n=" + n + "\n" +
                "MEL: min=" + minMel * 1e-9f + ", avg=" + totalMel * 1e-9f + "\n" +
                "AL: min=" + minAr * 1e-9f + ", avg=" + totalAr * 1e-9f);
    }


    private static void testPerformanceVec3RandomSwapping(int n) throws Exception {
        List<Vec3> mel = new MemoryEfficientList(Vec3.class, n);
        List<Vec3> al = new ArrayList<>(n);

        long minMel = Long.MAX_VALUE;
        long minAr = Long.MAX_VALUE;
        long totalMel = 0;
        long totalAr = 0;
        int ignored = 5;
        int count = 10;

        Random r = new Random();

        for(int h = 0; h < ignored + count; h++) {
            mel.clear();
            al.clear();

            r.setSeed(3453453464532409L);
            for(int i = 0; i < n; i++) {
                Vec3 lo = new Vec3();
                lo.x = r.nextFloat() * 100.0f;
                lo.y = r.nextFloat() * 100.0f;
                lo.z = r.nextFloat() * 100.0f;
                mel.add(lo);
                al.add(lo);
            }

            r.setSeed(1979804576454564L);
            long t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                Vec3 a = (Vec3) ((MemoryEfficientList) mel).get(pos1);
                Vec3 b = (Vec3) ((MemoryEfficientList) mel).get(pos2);
                mel.set(pos1, b);
                mel.set(pos2, a);
            }
            long deltaMel = System.nanoTime() - t;

            r.setSeed(1979804576454564L);
            t = System.nanoTime();
            for(int i = 0; i < n; i++) {
                int pos1 = r.nextInt(n);
                int pos2 = r.nextInt(n);
                Vec3 a = al.get(pos1);
                Vec3 b = al.get(pos2);
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

        System.out.println("Vec3RandomSwapping: n=" + n + "\n" +
                "MEL: min=" + minMel * 1e-9f + ", avg=" + totalMel * 1e-9f + "\n" +
                "AL: min=" + minAr * 1e-9f + ", avg=" + totalAr * 1e-9f);
    }


    public static void main(String[] args) throws Exception {
//        Thread.sleep(5_000);
//        testPerformanceIntegerToArray(10_000_000);
//        testPerformanceIntegerRandomSwapping(10_000_000);
//        testPerformancePrimes(10_000_000);
//        testPerformanceLargeObjectRandomSwapping(1_000_000);

        testPerformanceVec3RandomSwapping(10_000_000);

    }
}
