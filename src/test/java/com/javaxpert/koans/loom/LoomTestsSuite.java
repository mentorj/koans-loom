package com.javaxpert.koans.loom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

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
    
    

}