package com.aol.simple.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.lazy.LazyReact;

public class AutoclosingTest {

	
	@Test
	public void autoClosingLimit1() throws InterruptedException{
		close = new AtomicInteger();
		added = new AtomicInteger();
		//subscription fills from outside in (right to left), need to store open / closed for each queue
		List<String> results = new LazyReact().reactInfinitely(()->nextValues()).withQueueFactory(()-> eventQueue())
													  .flatMap(list -> list.stream())
													  .peek(System.out::println)
													  .flatMap(list -> list.stream())
													  .peek(System.out::println)
													  .limit(1)
													  .collect(Collectors.toList());
		System.out.println("finished");
		int localAdded = added.get();
		assertThat(close.get(),greaterThan(0));
		assertThat(results.size(),is(1));
		assertThat(localAdded,is(added.get()));
		
	}

	@Test 
	public void autoClosingLimit2Limit1() throws InterruptedException{
		System.out.println("Last test!!");
		close = new AtomicInteger();
		added = new AtomicInteger();
		//subscription fills from outside in (right to left), need to store open / closed for each queue
		List<String> results = new LazyReact().reactInfinitely(()->nextValues()).withQueueFactory(()-> eventQueue())
													  .flatMap(list -> list.stream())
													  .peek(System.out::println)
													  .limit(2)
													  .flatMap(list -> list.stream())
													  .peek(System.out::println)
													  .limit(1)
													  .collect(Collectors.toList());
		System.out.println("finished");
	
		
		
		int localAdded = added.get();
		assertThat(close.get(),greaterThan(0));
		assertThat(results.size(),is(1));
		assertThat(localAdded,is(added.get()));
		
	}
	@Test
	public void autoClosingZip() throws InterruptedException{
		close = new AtomicInteger();
		added = new AtomicInteger();
		//subscription fills from outside in (right to left), need to store open / closed for each queue
		List<Tuple2<List<List<String>>, Integer>> results = new LazyReact().reactInfinitely(()->nextValues()).withQueueFactory(()-> eventQueue())
													  .zip(LazyFutureStream.parallel(1,2,3))
													  .collect(Collectors.toList());
		System.out.println("finished");
	
		
		
		int localAdded = added.get();
		assertThat(close.get(),greaterThan(0));
		assertThat(results.size(),is(3));
		assertThat(localAdded,is(added.get()));
		
	}
	AtomicInteger added;
	AtomicInteger close;
	private Queue<List<List<String>>> eventQueue() {
		System.out.println("new event queue!");
		return new Queue(new LinkedBlockingQueue<>(100)){

			@Override
			public void closeAndClear() {
				close.incrementAndGet();
				super.closeAndClear();
			}
			
		};
		
	}

	private List<List<String>> nextValues() {
		added.incrementAndGet();
		return  asList(asList("1","2"),asList("1","2"));
	}
}