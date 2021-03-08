import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkingSortingAlgorithms
{
    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0 || args.length != 3) {
            System.out.println("Please input 3 parameters: [Thread Number] [Iteration] [size per thread/MB]");
            return;
        }

        final int threadNum = Integer.parseInt(args[0]);
        final int iteration = Integer.parseInt(args[1]);
        final int size = Integer.parseInt(args[2]) * 1024 * 1024;

        List<int[]> list = Collections.synchronizedList(new ArrayList<>());
        long[] sortTimeList = new long[threadNum];
        ExecutorService executorPool = Executors.newFixedThreadPool(threadNum);
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        System.out.println("============Start dataGen==========");
        long GenBegin = System.currentTimeMillis();
        for (int threadId = 0; threadId < threadNum; threadId++) {
            int i = threadId;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        list.add(createArrayWithRandomInts(size / 4));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            };
            executorPool.execute(runnable);
        }

        countDownLatch.await();
        long GenEnd = System.currentTimeMillis();
        System.out.println("===========finished dataGen========");
        System.out.println("Gen data time: " + (GenEnd - GenBegin) / 1000 + "s");

        CountDownLatch countDownLatch2 = new CountDownLatch(threadNum);
        System.out.println("============Start Sort=============");
        long sortBegin = System.currentTimeMillis();
        int threadId;
        for (threadId = 0; threadId < threadNum; threadId++) {
            int[] a = list.get(threadId);
            int i = threadId;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        long workerBegin = System.currentTimeMillis();
                        for(int j = 0; j < iteration; j++){
                            Arrays.sort(a.clone());
                        }
                        long workerEnd = System.currentTimeMillis();
                        long execution_time = workerEnd - workerBegin;
                        sortTimeList[i] = execution_time;
                        System.out.println("Thread " +  i + " Sort Time: " + execution_time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch2.countDown();
                    }
                }
            };
            executorPool.execute(runnable);
        }
        executorPool.shutdown();
        countDownLatch2.await();
        long sortFinish = System.currentTimeMillis();
        System.out.println("============Stop Sort===============");
        for (int i = 0; i < sortTimeList.length; i++) {
            System.out.println("Thread " + i + " Sort Time: " + sortTimeList[i] / 1000 + "s");
        }
        System.out.println("====================================");
        System.out.println("Total Sort Time: " + (sortFinish - sortBegin) / 1000 + "s");
    }

    /**
     * Creates and returns an array with random ints
     * @param size the size of the array to be created
     * @return
     */
    static int[] createArrayWithRandomInts(int size)
    {
        int bound = 100000;

        if (size > bound){
            bound = size;
        }
        int[] array = new int[size];
        Random r = new Random();
        for (int i = 0; i < size; i++)
        {
//            array[i] = (int) (Math.random() * Math.random() * bound);
            array[i] = r.nextInt(bound) + 1;
        }
        return array;
    }

    /**
     * Prints the elements of  one dimensional array of type int
     * @param array
     */
    static void printArray(int[] array)
    {
        for (int i = 0; i < array.length; i++)
        {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}