import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;


public class Apriori {

    private String src;

    private double percent;

    private int minSupport;

    private List<List<Integer>> database = new ArrayList<>();

    public Apriori(String src, double percent) {
        this.src = src;
        this.percent = percent;
    }

    public Map<String, Integer> getFreqItemSetCount() throws FileNotFoundException {
        Map<String, Integer> freqItemSetCount = new HashMap<>();
        int k = 1;
        int count = 0;
        List<List<Integer>> Ck = null;
        List<List<Integer>> Lk = readDataAndGetL1();
        while (!Lk.isEmpty()) {
            count += Lk.size();
            freqItemSetCount.put("L" + k++, Lk.size());
            Ck = Lk2Ck(Lk);
            Lk = Ck2Lk(Ck);
        }
        System.out.println("total=" + count);
        return freqItemSetCount;
    }

    private List<List<Integer>> readDataAndGetL1() throws FileNotFoundException {
        List<List<Integer>> L1 = new ArrayList<>();
        Map<Integer, Integer> itemFreq = new HashMap<>();
        Scanner sc = new Scanner(new DataInputStream(new FileInputStream(src)));
        while (sc.hasNextLine()) {
            String[] record = sc.nextLine().split(" ");
            List<Integer> transaction = new ArrayList<>();
            for (String item : record) {
                transaction.add(Integer.parseInt(item));
                itemFreq.put(Integer.parseInt(item), itemFreq.getOrDefault(Integer.parseInt(item), 0) + 1);
            }
            database.add(transaction);
        }
        this.minSupport = (int)(percent * database.size() + 99) / 100;
        for (Map.Entry<Integer, Integer> entry : itemFreq.entrySet()) {
            if (entry.getValue() >= minSupport) {
                L1.add(new ArrayList<>(Arrays.asList(entry.getKey())));
            }
        }
        sortLk(L1);
        return L1;
    }

    private List<List<Integer>> Lk2Ck(List<List<Integer>> Lk) {
        List<List<Integer>> Ck = new ArrayList<>();
        int llen = Lk.size();
        int ilen = Lk.get(0).size();
        for (int i = 0; i < llen; i++) {
            for (int j = i + 1; j < llen; j++) {
                List<Integer> itemSet1 = Lk.get(i);
                List<Integer> itemSet2 = Lk.get(j);
                List<Integer> prefix1 = itemSet1.subList(0, ilen - 1);
                List<Integer> prefix2 = itemSet2.subList(0, ilen - 1);
                if(prefix1.equals(prefix2)) {
                    Set<Integer> temp = new LinkedHashSet<>();
                    temp.addAll(itemSet1);
                    temp.addAll(itemSet2);
                    List<Integer> itemSet = new ArrayList<>(temp);
                    boolean flag = false;
                    for(int k = 0; k < itemSet.size() - 2; k++) {
                        List<Integer> tempCk = new ArrayList<>();
                        tempCk.addAll(itemSet.subList(0, k));
                        tempCk.addAll(itemSet.subList(k + 1, itemSet.size()));
                        if(!Lk.contains(tempCk)) {
                            flag = true;
                            break;
                        }
                    }
                    if(flag) continue;
                    Ck.add(itemSet);
                }
                else break;
            }
        }
        return Ck;
    }
    private List<List<Integer>> Ck2Lk(List<List<Integer>> Ck) {
        List<List<Integer>> Lk = new ArrayList<>();
        int clen = Ck.size();
        int[] count = new int[clen];
        for (List<Integer> transaction : database) {
            for (int j = 0; j < clen; j++) {
                List<Integer> itemSet = Ck.get(j);
                if (count[j] < minSupport && transaction.containsAll(itemSet)) {
                    count[j]++;
                    if (count[j] >= minSupport) {
                        Lk.add(itemSet);
                    }
                }
            }
        }
        sortLk(Lk);
        return Lk;
    }
    private void sortLk(List<List<Integer>> Lk) {
        Lk.sort((o1, o2) -> {
            int len = o1.size();
            for (int i = 0; i < len; i++) {
                if (!o1.get(i).equals(o2.get(i))) {
                    return o1.get(i) - o2.get(i);
                }
            }
            return 0;
        });
    }

    public static void main(String[] args) throws FileNotFoundException {
        long timestamp = System.currentTimeMillis();
        Apriori apriori = new Apriori("./retail.dat",  1);
        System.out.println(apriori.getFreqItemSetCount());
        System.out.println(System.currentTimeMillis() - timestamp + "ms");
    }
}
