package com.javaxpert.koans.loom;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this class exposes different runnables to be used in tests
 */
public class MultipleRunnables {
    Runnable shortLifeTask = () -> {
        System.out.println("This is a dummy task");
    };

    Runnable justFort1SecondTask = () -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Ephemeral task died after 1 s");
    };

    Runnable eternalTask = () -> {
    	while(true) {
    		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    };
    
    public static class LongRunningTask extends Thread{
        private volatile AtomicBoolean shouldBeStopped=new AtomicBoolean(false);

        public LongRunningTask(String name) {

            super(name);
            Runnable r = () -> {
              System.err.println("useless ? unsure");
            };
            Thread.ofVirtual().start(r);
        }

        /**
         * This method is run by the thread when it executes. Subclasses of {@code
         * Thread} may override this method.
         *
         * <p> This method is not intended to be invoked directly. If this thread is a
         * platform thread created with a {@link Runnable} task then invoking this method
         * will invoke the task's {@code run} method. If this thread is a virtual thread
         * then invoking this method directly does nothing.
         *
         * @implSpec The default implementation executes the {@link Runnable} task that
         * the {@code Thread} was created with. If the thread was created without a task
         * then this method does nothing.
         */
        @Override
        public void run() {
            System.out.println("starting dummy things");
            int counter =0;
            while(!shouldBeStopped.get()){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter++;
                if(counter%5==0)
                    System.out.println("Still working from  "+ Thread.currentThread().getName());
            }
            System.out.println("Long running thread finished");
        }
        public void toggleState(){
            System.out.println("Stopping running LongLifeTask");
            shouldBeStopped.compareAndSet(false,true);
        }
    }

    /**
     * get a handle to very short  lived task
     * @return {@code Rnnable{} unstarted
     */
    public Runnable getShortLifeTask(){
        return  shortLifeTask;
    }

    private LongRunningTask longLifeDummyThread = new LongRunningTask("Dummy");

    public LongRunningTask getLongLifeDummyThread(){
        return longLifeDummyThread;
    }

    public Runnable getJustFort1SecondTask(){
        return justFort1SecondTask;
    }
    /**
     * uses JMX to exhibit number of live threads (non virtual ones)
     * @return {@code Integer} number of live Java threads
     */
    public static int getActiveThreadsCount(){
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }


}
