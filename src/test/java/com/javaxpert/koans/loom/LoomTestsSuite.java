package com.javaxpert.koans.loom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Koans like tests suite
 */
class LoomTestsSuite {
    private MultipleRunnables runnableProvider;
    @BeforeEach
    void setUp() {
        runnableProvider= new MultipleRunnables();
    }

    @AfterEach
    void tearDown() {
        if(runnableProvider.getLongLifeDummyThread().getState()!= Thread.State.TERMINATED){
            runnableProvider.getLongLifeDummyThread().toggleState();
        }
    }

    @Test
    void startingLongRunningThreadShouldIncrementActiveThreadsCount(){
        // there 's a problem here , spot it!!!
        MultipleRunnables.LongRunningTask t = runnableProvider.getLongLifeDummyThread();
        int initialActiveThreads = MultipleRunnables.getActiveThreadsCount();
        t.start();
        int afterStartActiveThreads =MultipleRunnables.getActiveThreadsCount();
        assertEquals(true,t.isAlive());
        assertEquals(1,afterStartActiveThreads-initialActiveThreads);

    }

    @Test
    void startingVirtualThreadShouldNotIncreaseActiveThreadsCount(){
        int initialActiveThreads = MultipleRunnables.getActiveThreadsCount();
        Runnable r = runnableProvider.justFort1SecondTask;
        Thread t = Thread.ofVirtual().unstarted(r);
        t.start();

        int afterStartActiveThreads =MultipleRunnables.getActiveThreadsCount();
        assertEquals(true,t.isAlive());
        assertEquals(true,t.isVirtual());
        assertEquals(0,afterStartActiveThreads-initialActiveThreads);
    }

    @Test
    void virtualThreadDescSHouldContainPattern(){
        Runnable r = runnableProvider.shortLifeTask;
        Thread t = Thread.ofVirtual().unstarted(r);
        String thread_desc= t.toString();
        System.err.println("Thread desc ="+ thread_desc);
        assertTrue(thread_desc.contains("VirtualThrea") );
    }

    @Test
    void stoppingVirtualThreadsShouldRaiseException() {
    	Runnable r = runnableProvider.getJustFort1SecondTask();
    	Thread t = Thread.ofVirtual().start(r);
    	assertThrows(UnsupportedOperationException.class,() -> {t.stop();},"UnsuportedException expected here!!" );
    }
    
    @Test
    void whenNameMatters() {
    	Runnable r = runnableProvider.justFort1SecondTask;
    	ThreadFactory factory = Thread.ofVirtual().name("MyTask").factory();
    	
    	for (int i=0;i< 10000;i++) {

    		Thread t = factory.newThread(r);
    		t.start();
    		assertTrue(t.getName().contains("MyTask"));
    	}
    }
    
    @Test
    void changePriorityVirtualThreadsShouldHaveNoEffect() {
    	Runnable r = runnableProvider.getJustFort1SecondTask();
    	Thread t = Thread.startVirtualThread(r);
    	int initial_priority = t.getPriority();
    	t.setPriority(Thread.MAX_PRIORITY);
    	int after_priority = t.getPriority();
    	assertEquals(initial_priority, after_priority);
    	assertEquals(initial_priority, Thread.NORM_PRIORITY);
    }
    
    
    @Test
    void virtualThreadsDoNotSupportGroupNameOldFeature() {
    	Runnable r = runnableProvider.getJustFort1SecondTask();
    	Thread  t = Thread.ofVirtual().start(r);
    	int threads_in_group = t.getThreadGroup().activeCount();
    	System.err.println("Number of threads in group "+ threads_in_group);
    	assertEquals(0,threads_in_group);
    	
    }
    
    @Test
    void countingVirtualThreadsIsTrickyPart1() {
    	Runnable r = runnableProvider.eternalTask;	
    	long initialThreadsCount = ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
    	for(int i=0;i<100;i++) {
    		  Thread.ofVirtual().name("Eternal"+i).start(r);
    		  	
    	}
    	long afterCreationThreadsCount = ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
    	assertTrue(afterCreationThreadsCount>=initialThreadsCount,"Some new threads should have been created");
    	assertEquals(initialThreadsCount,afterCreationThreadsCount,"what is the number of threads created?");
    	
    }
    
    @Test
    void countingVirtualThreadsIsTrickyPart2() {
    	Runnable r = runnableProvider.eternalTask;	
    	for(int i=0;i<100;i++) {
    		Thread.ofVirtual().name("Eternal"+i).start(r);  
    	}
    	final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
    	long[] all_threads_id = threadMX.getAllThreadIds();
    	long active_virtual_threads =Arrays.stream(all_threads_id).mapToObj(l-> (Long)l)
    	.map(id -> threadMX.getThreadInfo(id))
    	.filter(info -> info.getThreadName().contains("Eternal"))
    	.count();
    	assertEquals(0l, active_virtual_threads,"expect to see as many Virtual Threads as started");
    }
    
    @Test
    void countingVirtualThreadsIsTrickyPart3() {
    	Runnable r = runnableProvider.eternalTask;	
    	for(int i=0;i<100;i++) {
    		 Thread.ofVirtual().name("Eternal"+i).start(r);	  	
    	}
    
		var stackTracesMap = Thread.getAllStackTraces();
		long vThhreadsNumber = stackTracesMap.keySet().stream().filter(thread -> thread.getName().contains("Eternal"))	.count();
		assertEquals(0l,vThhreadsNumber,"stacktrace should enable to find virtual threads");
    }
    
    
    @Test
    void executorServiceRefactoredToHostVirtualThreads() {
    	 final AtomicInteger counter = new AtomicInteger(0);
    	  try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    	    IntStream.range(0, 10000).forEach(i -> {
    	      executor.submit(() -> {
    	        try {
					Thread.sleep(Duration.ofSeconds(1));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    	        counter.incrementAndGet();
    	      });
    	      
    	    });
    	    try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	    assertEquals(10000,counter.get(),"Counter should host as many tasks launched");
    	  }
    	    
    }
    
    

}