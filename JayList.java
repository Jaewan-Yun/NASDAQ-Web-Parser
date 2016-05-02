/**
*	A circular implementation of an array-based deque.
*
*	@author Jaewan Yun (Jay50@pitt.edu)
*	@version 1.0.0
*/

import java.util.*;
import constant.*;
import static constant.Keyword.*;

public class JayList<T> implements MyDeque<T>
{
	// underlying data structure.
	private volatile T[] jayList = null;

	// data structure settings.
	private final int DEFAULT_CAPACITY = 2;	//e.g. 1024
	private final double EXPANSION_FACTOR = 2.0;
	private final double REDUCTION_FACTOR = 2.0;
	private final int REDUCTION_REQUIREMENT_SIZE = 2;	//e.g. 1025
	private final int REDUCTION_REQUIREMENT_FACTOR = 4;	//e.g. 4
	private final int MAX_CAPACITY = (2147483647 / (int) EXPANSION_FACTOR);

	// class states.
	private static volatile int concurrentObjects = 0;
	private static volatile long concurrentCapacity = 0;
	private static volatile long concurrentSize = 0;

	// array states.
	private volatile int size = 0;
	private volatile int capacity = 0;

	private volatile boolean initialized = false;

	// note that cursor does not indicate index.
	private volatile int headCursor = 0;
	private volatile int tailIndex = 0;

	/**
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public JayList()
	{
		synchronized(this)
		{
			initialized = true;
			jayList = constructArray(DEFAULT_CAPACITY);
			capacity = DEFAULT_CAPACITY;
			synchronized(this.getClass())
			{
				concurrentCapacity += DEFAULT_CAPACITY;
				concurrentObjects++;
			}
		}
	}

	/**
	*	@param capacity The desired capacity of the underlying data structure.
	*	@throws IllegalArgumentException when the size of the accepted value exceeds a predetermined maximum capacity.
	*	@throws IllegalArgumentException when the size of the accepted value is less than one.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public JayList(int capacity)
	{
		synchronized(this)
		{
			initialized = true;
			jayList = constructArray(capacity);
			this.capacity = capacity;
			synchronized(this.getClass())
			{
				concurrentCapacity += capacity;
				concurrentObjects++;
			}
		}
	}

	/**
	*	@param input An array used as a template.
	*	@return true when storage was successful, and false if otherwise.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public JayList(T[] input)
	{
		synchronized(this)
		{
			initialized = true;
			setArray(input, input.length);
			synchronized(this.getClass())
			{
				concurrentObjects++;
			}
		}
	}

	/**
	*	@param entry An entry to be added.
	*	@throws IllegalStateException when this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	*	@throws IllegalArgumentException when entry is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T addFirst(T entry)
	{
		return addFirst(entry, NULL);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param entry An entry to be added.
	*	@param keyword Used for development.
	*	@throws IllegalStateException when this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	*	@throws IllegalArgumentException when entry is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T addFirst(T entry, Keyword keyword)
	{
		if(entry == null)
			throw new IllegalArgumentException();

		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to ADD FIRST : CAPACITY : " + capacity);
			print(1, "Prior to ADD FIRST : HEADCURSOR : " + headCursor);
			print(1, "Prior to ADD FIRST : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to ADD FIRST : SIZE : " + size + " ADDING " + entry + "(" + entry.getClass().toString() + ")");
		}
		// END DEBUG


		// add the entry to the headCursor position and increment headCursor using modulo.
		checkInitialization();
		if(isFull())
			increaseCapacity(EXPANSION_FACTOR, keyword, -1);
		jayList[headCursor] = entry;
		headCursor = (headCursor + 1) % capacity;
		size++;
		synchronized(this.getClass())
		{
			concurrentSize++;
		}

		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "After ADD FIRST : CAPACITY : " + capacity);
			print(1, "After ADD FIRST : HEADCURSOR : " + headCursor);
			print(1, "After ADD FIRST : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After ADD FIRST : SIZE : " + size + " ADDED " + entry + "(" + entry.getClass().toString() + ")");
		}
		// END DEBUG

		if(headCursor == 0)
			return jayList[capacity - 1];
		else
			return jayList[headCursor - 1];
	}

	/**
	*	@param entry An entry to be added.
	*	@throws IllegalStateException when this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	*	@throws IllegalArgumentException when entry is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T addLast(T entry)
	{
		return addLast(entry, NULL);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param entry An entry to be added.
	*	@param keyword Used for development.
	*	@throws IllegalStateException when this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	*	@throws IllegalArgumentException when entry is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T addLast(T entry, Keyword keyword)
	{
		if(entry == null)
			throw new IllegalArgumentException();

		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to ADD LAST : CAPACITY : " + capacity);
			print(1, "Prior to ADD LAST : HEADCURSOR : " + headCursor);
			print(1, "Prior to ADD LAST : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to ADD LAST : SIZE : " + size + " ADDING " + entry + "(" + entry.getClass().toString() + ")");
		}
		// END DEBUG


		checkInitialization();
		if(isFull())
			increaseCapacity(EXPANSION_FACTOR, keyword, -1);

		if(tailIndex == 0)
		{
			tailIndex = capacity - 1;
			jayList[tailIndex] = entry;
		}
		else
		{
			jayList[--tailIndex] = entry;
		}
		size++;
		synchronized(this.getClass())
		{
			concurrentSize++;
		}


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "After ADD LAST : CAPACITY : " + capacity);
			print(1, "After ADD LAST : HEADCURSOR : " + headCursor);
			print(1, "After ADD LAST : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After ADD LAST : SIZE : " + size + " ADDED " + entry + "(" + entry.getClass().toString() + ")");
		}
		// END DEBUG

		return jayList[tailIndex];
	}

	/**
	*	@param entry An entry to be added.
	*	@param position The relative position (not the underlying index) at which the entry will be inserted into.
	*	@throws IllegalStateException when this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	*	@throws IllegalArgumentException when entry is null.
	*	@since 1..0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T add(T entry, int position)
	{
		return add(entry, NULL, position);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param entry An entry to be added.
	*	@param keyword Used for development.
	*	@param position The relative position (not the underlying index) at which the entry will be inserted into.
	*	@throws IllegalStateException when this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	*	@throws IllegalArgumentException when entry is null or if the position is not valid.
	*	@since 1.1.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T add(T entry, Keyword keyword, int position)
	{
		if(entry == null || position < 0 || position >= size)
			throw new IllegalArgumentException();

		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to ADD POSITION : CAPACITY : " + capacity);
			print(1, "Prior to ADD POSITION : HEADCURSOR : " + headCursor);
			print(1, "Prior to ADD POSITION : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to ADD POSITION : SIZE : " + size + " ADDING " + entry + "(" + entry.getClass().toString() + ")");
		}
		// END DEBUG


		T toReturn = null;
		checkInitialization();
		if(isFull())
		{
			increaseCapacity(EXPANSION_FACTOR, keyword, position);
			jayList[position] = entry;
		}
		else
		{
			// shift first or last positions into last or first positions until position is met, respectively, and insert at the open position.
			if(position < (size / 2))
			{
				for(int j = 0; j < position; j++)
				{
					addLast(removeFirst());
				}
				toReturn = addFirst(entry);
				// TODO
				for(int j = 0; j < position; j++)
				{
					addFirst(removeLast());
				}
			}
			else
			{
				for(int j = 0; j < position; j++)
				{
					addFirst(removeLast());
				}
				toReturn = addLast(entry);
				// TODO
				for(int j = 0; j < position; j++)
				{
					addLast(removeFirst());
				}
			}
		}
		size++;
		synchronized(this.getClass())
		{
			concurrentSize++;
		}


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "After ADD POSITION : CAPACITY : " + capacity);
			print(1, "After ADD POSITION : HEADCURSOR : " + headCursor);
			print(1, "After ADD POSITION : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After ADD POSITION : SIZE : " + size + " ADDED " + entry + "(" + entry.getClass().toString() + ")");
		}
		// END DEBUG

		// TODO
		return toReturn;
	}

	/**
	*	@return the element that was removed.
	*	@throws IllegalArgumentException if data structure is empty.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T removeLast()
	{
		return removeLast(NULL);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param keyword Used for development.
	*	@return the element that was removed.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if removed value is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T removeLast(Keyword keyword)
	{
		// check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to REMOVE LAST : CAPACITY : " + capacity);
			print(1, "Prior to REMOVE LAST : HEADCURSOR : " + headCursor);
			print(1, "Prior to REMOVE LAST : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to REMOVE LAST : SIZE : " + size + " REMOVING " + jayList[tailIndex] + "(" + jayList[tailIndex].getClass().toString() + ")");
		}
		// END DEBUG


		// remove an item from the tailIndex and increment tailIndex using modulo.
		T toReturn = jayList[tailIndex];
		jayList[tailIndex] = null;
		tailIndex = ++tailIndex % capacity;
		size--;
		synchronized(this.getClass())
		{
			concurrentSize--;
		}


		// DEBUG
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After REMOVE LAST : SIZE : " + size + " REMOVED " + toReturn + "(" + toReturn.getClass().toString() + ")");
		}
		// END DEBUG


		// reduce capacity.
		if((size < (capacity / REDUCTION_REQUIREMENT_FACTOR)) && (capacity > REDUCTION_REQUIREMENT_SIZE))
			decreaseCapacity(REDUCTION_FACTOR, keyword);


		// // DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "After REMOVE LAST : CAPACITY : " + capacity);
			print(1, "After REMOVE LAST : HEADCURSOR : " + headCursor);
			print(1, "After REMOVE LAST : TAILINDEX : " + tailIndex);
		}
		// // END DEBUG

		if(toReturn == null)
			throw new NullPointerException();
		return toReturn;
	}

	/**
	*	@return the element that was popped.
	*	@throws IllegalArgumentException if data structure is empty.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T removeFirst()
	{
		return removeFirst(NULL);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param keyword Used for development.
	*	@return the element that was popped.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if popped value is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T removeFirst(Keyword keyword)
	{
		// check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to REMOVE FIRST: CAPACITY : " + capacity);
			print(1, "Prior to REMOVE FIRST: HEADCURSOR : " + headCursor);
			print(1, "Prior to REMOVE FIRST: TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to REMOVE FIRST: SIZE : " + size + " REMOVING " + jayList[headCursor - 1] + "(" + jayList[headCursor - 1].getClass().toString() + ")");
		}
		// END DEBUG


		T toReturn;
		if(headCursor == 0)
		{
			headCursor = capacity - 1;
			toReturn = jayList[headCursor];
			jayList[headCursor] = null;
		}
		else
		{
			toReturn = jayList[--headCursor];
			jayList[headCursor] = null;
		}
		size--;
		synchronized(this.getClass())
		{
			concurrentSize--;
		}


		// DEBUG
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After REMOVE FIRST: SIZE : " + size + " REMOVED " + toReturn + "(" + toReturn.getClass().toString() + ")");
		}
		// END DEBUG


		// reduce capacity.
		if((size < (capacity / REDUCTION_REQUIREMENT_FACTOR)) && (capacity > REDUCTION_REQUIREMENT_SIZE))
			decreaseCapacity(REDUCTION_FACTOR, keyword);


		// // DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "After REMOVE FIRST : CAPACITY : " + capacity);
			print(1, "After REMOVE FIRST : HEADCURSOR : " + headCursor);
			print(1, "After REMOVE FIRST : TAILINDEX : " + tailIndex);
		}
		// // END DEBUG

		if(toReturn == null)
			throw new NullPointerException();
		return toReturn;
	}

	/**
	*	@param position The relative position (not the underlying index) at which the entry will be removed from.
	*	@throws IllegalStateException when this has not been properly initialized.
	*	@throws IllegalArgumentException if the position is not valid.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if popped value is null.
	*	@since 1.1.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T remove(int position)
	{
		return remove(NULL, position);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param keyword Used for development.
	*	@param position The relative position (not the underlying index) at which the entry will be removed from.
	*	@throws IllegalStateException when this has not been properly initialized.
	*	@throws IllegalArgumentException if the position is not valid.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if popped value is null.
	*	@since 1.1.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T remove(Keyword keyword, int position)
	{
		if(position < 0 || position >= size)
			throw new IllegalArgumentException();

		// check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to REMOVE POSITION: CAPACITY : " + capacity);
			print(1, "Prior to REMOVE POSITION: HEADCURSOR : " + headCursor);
			print(1, "Prior to REMOVE POSITION: TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to REMOVE POSITION: SIZE : " + size + " REMOVING " + jayList[position] + "(" + jayList[position].getClass().toString() + ")");
		}
		// END DEBUG


		T toReturn = null;
		checkInitialization();
		// shift first or last positions into last or first positions until position is met, respectively, and insert at the open position.
		if(position < (size / 2))
		{
			for(int j = 0; j < position; j++)
			{
				addLast(removeFirst());
			}
			toReturn = removeFirst();
			// TODO
			for(int j = 0; j < position; j++)
			{
				addFirst(removeLast());
			}
		}
		else
		{
			for(int j = 0; j < position; j++)
			{
				addFirst(removeLast());
			}
			toReturn = removeLast();
			// TODO
			for(int j = 0; j < position; j++)
			{
				addLast(removeFirst());
			}
		}
		size--;
		synchronized(this.getClass())
		{
			concurrentSize--;
		}


		// DEBUG
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After REMOVE POSITION: SIZE : " + size + " REMOVED " + toReturn + "(" + toReturn.getClass().toString() + ")");
		}
		// END DEBUG


		// reduce capacity.
		if((size < (capacity / REDUCTION_REQUIREMENT_FACTOR)) && (capacity > REDUCTION_REQUIREMENT_SIZE))
			decreaseCapacity(REDUCTION_FACTOR, keyword);


		// // DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "After REMOVE POSITION : CAPACITY : " + capacity);
			print(1, "After REMOVE POSITION : HEADCURSOR : " + headCursor);
			print(1, "After REMOVE POSITION : TAILINDEX : " + tailIndex);
		}
		// // END DEBUG

		if(toReturn == null)
			throw new NullPointerException();
		return toReturn;
	}

	/**
	*	@return the element that is next in queue.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if next value is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T getLast()
	{
		return getLast(NULL);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param keyword Used for development.
	*	@return the element that is next in queue.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if next value is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T getLast(Keyword keyword)
	{
		// check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to ELEMENT : CAPACITY : " + capacity);
			print(1, "Prior to ELEMENT : HEADCURSOR : " + headCursor);
			print(1, "Prior to ELEMENT : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to ELEMENT : SIZE : " + size + " ELEMENTING " + jayList[tailIndex] + "(" + jayList[tailIndex].getClass().toString() + ")");
		}
		// END DEBUG


		// get next.
		T toReturn = jayList[tailIndex];


		// DEBUG
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After ELEMENT : SIZE : " + size + " ELEMENTED " + toReturn + "(" + toReturn.getClass().toString() + ")");
		}
		if(keyword == IDDEBUG)
		{
			print(1, "After ELEMENT : CAPACITY : " + capacity);
			print(1, "After ELEMENT : HEADCURSOR : " + headCursor);
			print(1, "After ELEMENT : TAILINDEX : " + tailIndex);
		}
		// // END DEBUG


		if(toReturn == null)
			throw new NullPointerException();
		return toReturn;
	}

	/**
	*	@return the element that is next in stack.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if next value is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public T getFirst()
	{
		return getFirst(NULL);
	}
	/**
	*	Bottleneck synchronized with this.
	*
	*	@param keyword Used for development.
	*	@return the element that is next in stack.
	*	@throws NoSuchElementException if data structure is empty.
	*	@throws NullPointerException if next value is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized T getFirst(Keyword keyword)
	{
		// check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();


		// DEBUG
		if(keyword == IDDEBUG)
		{
			print(1, "Prior to PEEK : CAPACITY : " + capacity);
			print(1, "Prior to PEEK : HEADCURSOR : " + headCursor);
			print(1, "Prior to PEEK : TAILINDEX : " + tailIndex);
		}
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "Prior to PEEK : SIZE : " + size + " PEEKING " + jayList[tailIndex] + "(" + jayList[tailIndex].getClass().toString() + ")");
		}
		// END DEBUG


		// get next.
		T toReturn = jayList[(headCursor - 1) % capacity];


		// DEBUG
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "After PEEK : SIZE : " + size + " PEEKED " + toReturn + "(" + toReturn.getClass().toString() + ")");
		}
		if(keyword == IDDEBUG)
		{
			print(1, "After PEEK : CAPACITY : " + capacity);
			print(1, "After PEEK : HEADCURSOR : " + headCursor);
			print(1, "After PEEK : TAILINDEX : " + tailIndex);
		}
		// // END DEBUG


		if(toReturn == null)
			throw new NullPointerException();
		return toReturn;
	}

	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@param factor The multiplicative expansion coefficient.
	*	@param keyword Used for development.
	*	@throws IllegalArgumentException when capacity cannot increase due to a predetermined maximum capacity.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	private void increaseCapacity(double factor, Keyword keyword, int position)
	{
		// DEBUG
		if(keyword == DEBUG)
		{
			print(1, "\nSIZE : " + size + " out of " + capacity);
			print(1, "INCREASING CAPACITY...");
			int count = 0;
			for(int j = 0; j < jayList.length; j++)
			{
				print(0, "\tPOS " + j + " > ");
				if(jayList[j] != null)
				{
					print(1, jayList[j] + "(" + jayList[j].getClass().toString() + ")");
					count++;
				}
				else
				{
					print(1, "null");
				}
			}
			print(1, "objectCounter : " + count);
		}
		// END DEBUG


		// increase capacity.
		if((int) (capacity * EXPANSION_FACTOR + 1) > MAX_CAPACITY)
			throw new IllegalStateException();
		synchronized(this.getClass())
		{
			concurrentCapacity -= capacity;
		}
		int originalCapacity = capacity;
		capacity = (int) (capacity * factor);
		synchronized(this.getClass())
		{
			concurrentCapacity += capacity;
		}
		T[] temporaryRef = constructArray(capacity);

		if(position == -1 || position == (size - 1))
		{
			for(int j = 0; j < size; j++)
			{
				temporaryRef[j] = jayList[tailIndex % originalCapacity];
				tailIndex++;
			}
			tailIndex = 0;
			headCursor = size;
			jayList = temporaryRef;
		}
		else
		{
			for(int j = 0; j <= size; j++)
			{
				if(j == position)
				{
					tailIndex--;
				}
				temporaryRef[j] = jayList[tailIndex % originalCapacity];
				tailIndex++;
			}
			tailIndex = 0;
			headCursor = size + 1;
			jayList = temporaryRef;
		}

		// DEBUG
		if(keyword == DEBUG)
		{
			print(1, "CAPACITY INCREASED TO : " + capacity + "\n");
		}
		// END DEBUG
	}

	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@param factor The multiplicative reduction coefficient.
	*	@throws IllegalArgumentException when capacity cannot increase due to a predetermined maximum capacity.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	private void decreaseCapacity(double factor, Keyword keyword)
	{
		// DEBUG
		if(keyword == DEBUG)
		{
			for(int j = 0; j < 10; j++)
			{
				print(1, "\n");
			}
			print(1, "\nSIZE : " + size + " out of " + capacity);
			int count = 0;
			for(int j = 0; j < jayList.length; j++)
			{
				print(0, "\tPOS " + j + " > ");
				if(jayList[j] != null)
				{
					print(1, jayList[j] + "(" + jayList[j].getClass().toString() + ")");
					count++;
				}
				else
				{
					print(1, "null");
				}
			}
			print(1, "objectCounter : " + count);
			for(int j = 0; j < 10; j++)
			{
				print(1, "\n");
			}
		}
		// END DEBUG


		// decrease capacity.
		int originalCapacity = capacity;
		synchronized(this.getClass())
		{
			concurrentCapacity -= capacity;
		}
		capacity = (int) (capacity / factor);
		synchronized(this.getClass())
		{
			concurrentCapacity += capacity;
		}
		T[] temporaryRef = constructArray(capacity);
		for(int j = 0; j < capacity - 1; j++)
		{
			temporaryRef[j] = jayList[tailIndex++ % originalCapacity];
		}
		tailIndex = 0;
		headCursor = size;
		jayList = temporaryRef;


		// DEBUG
		if(keyword == DEBUG)
		{
			print(1, "\nSIZE : " + size + " out of " + capacity);
			print(1, "DECREASING CAPACITY...");
			int count = 0;
			for(int j = 0; j < jayList.length; j++)
			{
				print(0, "\tPOS " + j + " > ");
				if(jayList[j] != null)
				{
					print(1, jayList[j] + "(" + jayList[j].getClass().toString() + ")");
					count++;
				}
				else
				{
					print(1, "null");
				}
			}
			print(1, "objectCounter : " + count);
			print(1, "CAPACITY DECREASED TO : " + capacity + "\n");
		}
		// END DEBUG
	}

	/**
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized void clear()
	{
		jayList = null;
		jayList = constructArray(DEFAULT_CAPACITY);
		synchronized(this.getClass())
		{
			concurrentCapacity -= (capacity - DEFAULT_CAPACITY);

		}
		capacity = DEFAULT_CAPACITY;
		synchronized(this.getClass())
		{
			concurrentSize -= size;
			size = 0;
		}
		headCursor = 0;
		tailIndex = 0;
	}

	public synchronized boolean setArray(T[] input)
	{
		return setArray(input, input.length);
	}
	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@param input An array used as a template.
	*	@return true when storage was successful, and false if otherwise.
	*	@throws IllegalStateException when this has not been properly initialized.
	*	@throws IllegalArgumentException when capacity cannot increase due to a predetermined maximum capacity.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	private boolean setArray(T[] input, int length)
	{
		checkInitialization();

		if(input == null)
		{
			return false;
		}

		if(length + 1 > MAX_CAPACITY)
			throw new IllegalArgumentException();


		jayList = constructArray(length + 1);
		capacity = length + 1;
		synchronized(this.getClass())
		{
			concurrentCapacity -= jayList.length;
			concurrentCapacity += length + 1;
		}

		// copy references
		synchronized(this.getClass())
		{
			concurrentSize -= size;
			size = 0;
		}
		for(int j = 0; j < length; j++)
		{
			if(input[j] != null)
			{
				jayList[j] = input[j];
				size++;
			}
		}
		synchronized(this.getClass())
		{
			concurrentSize += size;
		}
		tailIndex = 0;
		headCursor = length;
		return true;
	}

	/**
	*	@return A copy of this array.
	*	@throws IllegalStateException when this has not been properly initialized.
	*	@throws NullPointerException when jayList is null.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	@SuppressWarnings("unchecked") public synchronized T[] toArray()
	{
		checkInitialization();
		int newTailIndex = tailIndex;
		T[] toReturn = (T[]) new Object[size];
		for(int j = 0; j < size; j++)
		{
			toReturn[j] = jayList[newTailIndex++ % capacity];
		}
		return toReturn;
	}

	/**
	*	@param toCopy An array used as a template.
	*	@return A copy of the accepted array.
	*	@throws NullPointerException when the accepted array is null.
	*	@throws IllegalArgumentException when the size of the accepted array exceeds a predetermined maximum capacity.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	@SuppressWarnings("unchecked") private T[] copyOf(T[] toCopy)
	{
		synchronized(this)
		{
			if(toCopy == null)
			{
				throw new NullPointerException();
			}

			if(toCopy.length > MAX_CAPACITY)
			{
				throw new IllegalArgumentException();
			}

			// copy the accepted array
			T[] toReturn = (T[]) new Object[toCopy.length];
			for(int j = 0; j < toCopy.length; j++)
			{
				toReturn[j] = toCopy[j];
			}
			return toReturn;
		}
	}

	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@param capacity The capacity of the array to be constructed.
	*	@return Initialized array of T types with the accepted value as its capacity.
	*	@throws IllegalArgumentException when the size of the accepted value exceeds a predetermined maximum capacity.
	*	@throws IllegalArgumentException when the size of the accepted value is less than one.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	@SuppressWarnings("unchecked") private T[] constructArray(int capacity)
	{
		if(capacity > MAX_CAPACITY || capacity < 1)
		{
			throw new IllegalArgumentException();
		}

		// initialize an array of type T
		T[] toReturn = (T[]) new Object[capacity];

		// setting the states
		initialized = true;
		return toReturn;
	}

	/**
	*	Client ensures object types are comparable.
	*
	*	@throws UnsupportedOperationException if object types are not comparable.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized void sort()
	{
		try
		{
			T[] a = toArray();
			Arrays.sort(a);
			setArray(a);
		}
		catch(Exception e)
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	*	@return size The number of elements contained within this data structure.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized int size()
	{
		return size;
	}

	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@throws IllegalStateException when this has not been properly initialized.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	private void checkInitialization()
	{
		if(!initialized)
		{
			throw new IllegalStateException();
		}
	}

	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@return true if no elements exist in this data structure.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public boolean isEmpty()
	{
		if(headCursor == tailIndex)
			return true;
		return false;
	}

	/**
	*	Client method needs to ensure synchronization with this.
	*
	*	@return true if data represented is in full state.
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	private boolean isFull()
	{
		if(((headCursor + 1) % capacity) == tailIndex)
			return true;
		return false;
	}

	/**
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	protected void finalize()
	{
		synchronized(this)
		{
			initialized = false;
			synchronized(this.getClass())
			{
				concurrentObjects--;
				concurrentCapacity -= capacity;
			}
		}
	}

	/**
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized String toString()
	{
		return jayList.toString();
	}

	/**
	*	@param keyword Keyword that the method body portion execution is dependent on
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	public synchronized void showState(Keyword keyword)
	{
		if(keyword == DEBUG || keyword == IDDEBUG)
		{
			print(1, "jayList Address :\t" + jayList);
			print(1, "MAX_CAPACITY :\t\t" + MAX_CAPACITY);
			print(1, "DEFAULT_CAPACITY :\t" + DEFAULT_CAPACITY);
			print(1, "EXPANSION_FACTOR :\t" + EXPANSION_FACTOR);
			print(1, "REDUCTION_FACTOR :\t" + REDUCTION_FACTOR);
			print(1, "concurrentObjects :\t" + concurrentObjects);
			print(1, "concurrentCapacity :\t" + concurrentCapacity);
			print(1, "concurrentSize : \t" + concurrentSize);
			print(1, "size :\t\t\t" + size + " <---");
			print(1, "capacity :\t\t" + capacity + " <---");
			print(1, "initialized :\t\t" + initialized);
			print(1, "headCursor :\t\t" + headCursor);
			print(1, "tailIndex :\t\t" + tailIndex);
			print(1, "\n\tEND OF JayList EXPLICIT STATE\n");
		}

		if(keyword == IDDEBUG)
		{
			if(jayList != null)
			{
				print(1, "length :\t\t" + jayList.length);
				if(jayList[tailIndex] != null)
					print(1, "tailIndex type :\t" + jayList[tailIndex].getClass().toString());
				else
					print(1, "tailIndex type :\tnull");
				if(jayList[headCursor] != null)
					print(1, "headCursor type :\t" + jayList[tailIndex].getClass().toString());
				else
					print(1, "headCursor type :\tnull");
				if(headCursor - 1 < 0)
					if(jayList[capacity - 1] != null)
						print(1, "headIndex type :\t" + jayList[tailIndex].getClass().toString());
				if(headCursor - 1 >= 0)
					if(jayList[headCursor - 1] != null)
						print(1, "headIndex type :\t" + jayList[tailIndex].getClass().toString());
				print(1, "\n\tEND OF T[] EXPLICIT STATE\n");

				for(int j = 0; j < jayList.length; j++)
				{
					print(0, "Index  " + j + ": \t[" + jayList[j]);
					if(jayList[j] != null)
						print(1, "\t] of type (" + jayList[j].getClass().toString() + ")");
					else
						print(0, "\t]\n");
				}
				print(1, "\n\tEND OF T[] ENUMERATION");
			}
			else
			{
				print(2, "jayList is null therefore unaccessible");
			}
		}
	}

	/**
	*	@since 1.0.0
	*	@author Jaewan Yun (Jay50@pitt.edu)
	*/
	private void print(int skip, String toPrint)
	{
		System.out.print(toPrint);

		if(skip == 0)
		{
			return;
		}

		for(int j = 0; j < skip; j++)
		{
			System.out.print("\n");
		}
	}
}