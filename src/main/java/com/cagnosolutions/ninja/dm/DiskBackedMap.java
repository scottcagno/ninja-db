package com.cagnosolutions.ninja.dm;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */


public class DiskBackedMap<K, V> extends AbstractMap<K, V> implements Closeable {

	/**
	 * Public constants
	 */

	public static final String INDEX_FILE_SUFFIX = ".ix";
	public static final String DATA_FILE_SUFFIX = ".db";

	/**
	 * Private constants
	 */

	private static final String VERSION_STAMP = "DiskBackedMap-0.0.1_BETA";

	/**
	 * Private inner classes, supporting the DiskBackedMap
	 */

	// Wraps the values data file and other administrative references related to it.
	private static class ValuesFile {
		private RandomAccessFile file;
		ValuesFile(File f) throws IOException {
			this.file = new RandomAccessFile(f, "rw");
		}
		RandomAccessFile getFile() throws ConcurrentModificationException {
			return file;
		}
		void close() throws IOException {
			file.close(); // lock implicitly released on close
		}
	}

	// Comparator for DiskBackedMapEntry objects. Sorts by natural order (file position)
	private class DiskBackedMapEntryComparator implements Comparator<DiskBackedMapEntry<K>> {
		private DiskBackedMapEntryComparator() {}
		public int compare(DiskBackedMapEntry<K> o1, DiskBackedMapEntry<K> o2) {
			return o1.compareTo(o2);
		}
		public boolean equals(Object o) {
			return (this.getClass().isInstance(o));
		}
		public int hashCode() {
			return super.hashCode();
		}
	}

	// Comparator for DiskBackedMapEntry objects. Sorts by object size, then file position. (for ordering gap entries)
	private class DiskBackedMapEntryGapComparator implements Comparator<DiskBackedMapEntry<K>> {
		private DiskBackedMapEntryGapComparator() {}
		public int compare(DiskBackedMapEntry<K> o1, DiskBackedMapEntry<K> o2) {
			int cmp;
			if((cmp = o1.getObjectSize() - o2.getObjectSize()) == 0)
				cmp = (int) (o1.getFilePosition() - o2.getFilePosition());
			return cmp;
		}
		public boolean equals(Object o) {
			return (this.getClass().isInstance(o));
		}
		public int hashCode() {
			return super.hashCode();
		}
	}

	// Internal iterator that loops through the DiskBackedMapEntry objects in sorted order, by file position.
	// Used to implement other iterators.
	private class EntryIterator implements Iterator<DiskBackedMapEntry<K>> {
		List<DiskBackedMapEntry<K>> entries;
		Iterator<DiskBackedMapEntry<K>> iterator;
		DiskBackedMapEntry<K> currentEntry = null;
		// The expectedSize value that the iterator believes that the backing map should have.
		// If this expectation is violated, the iterator has detected concurrent modification.
		private int expectedSize = 0;
		EntryIterator() {
			entries = DiskBackedMap.this.getSortedEntries();
			iterator = entries.iterator();
			expectedSize = entries.size();
		}
		public boolean hasNext() {
			return iterator.hasNext();
		}
		public DiskBackedMapEntry<K> next() {
			if (expectedSize != DiskBackedMap.this.indexMap.size())
				throw new ConcurrentModificationException();
			if (hasNext())
				currentEntry = iterator.next();
			else {
				currentEntry = null;
				throw new NoSuchElementException();
			}
			return currentEntry;
		}
		public void remove() {
			if((currentEntry != null) && (expectedSize > 0)) {
				K key = currentEntry.getKey();
				V value = DiskBackedMap.this.remove(key);
				if (value != null) {
					if (hasNext())
						currentEntry = iterator.next();
					else
						currentEntry = null;
				}
			}
		}
	}

	// Specialized iterator iterates through the value file sequentially (sorted by file position), and
	// demand-loads the values (call to next()), so they're not all loaded into memory at the same time.
	private class ValueIterator implements Iterator<V> {
		EntryIterator it;
		private ValueIterator() {
			it = new EntryIterator();
		}
		public boolean hasNext() {
			return it.hasNext();
		}
		public V next() {
			return DiskBackedMap.this.readValueNoError(it.next());
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	// Shallow set that contains pointers to the on-disk values. (only loaded when referenced)
	private class ValueSet extends AbstractSet<V> {
		ValuesFile valuesDB = DiskBackedMap.this.valuesDB;
		private ValueSet() {}
		public void clear() {
			throw new UnsupportedOperationException();
		}
		public boolean contains(Object o) {
			boolean containsIt = false;
			seekTo(0); // Loop through values sequentially, to optimize file access.
			for (Iterator<V> it = new ValueIterator(); it.hasNext(); ) {
				V obj = it.next();
				if (obj.equals(o)) {
					containsIt = true;
					break;
				}
			}
			return containsIt;
		}
		public boolean containsAll(Collection c) {
			boolean containsThem = true;
			// Loop through values sequentially, to optimize file access.
			seekTo(0);
			for (Iterator<V> it = new ValueIterator(); it.hasNext(); ) {
				V obj = it.next();
				if (!c.contains(obj)) {
					containsThem = false;
					break;
				}
			}
			return containsThem;
		}
		public boolean equals(Object o) {
			boolean eq = false;
			Set other = (Set) o;
			if (other.size() == size())
				eq = this.containsAll(other);
			return eq;
		}
		public int hashCode() {
			int result = 0;
			seekTo(0); // Loop through values sequentially, to optimize file access.
			for (Iterator<V> it = new ValueIterator(); it.hasNext(); ) {
				Object obj = it.next();
				result += obj.hashCode();
			}
			return result;
		}
		public boolean isEmpty() {
			return indexMap.isEmpty();
		}
		public Iterator<V> iterator() {
			return new ValueIterator();
		}
		public boolean remove(Object o) {
			return (DiskBackedMap.this.remove(o) != null);
		}
		public int size() {
			return currentSize();
		}
		private void seekTo(long pos) {
			try {
				valuesDB.getFile().seek(pos);
			} catch (IOException ex) {
				System.err.printf("ValueSet.seekTo(pos) -> %s\n", ex);
			}
		}
	}

	// Necessary to support the Map.entrySet() routine. Each object returned by DiskBackedMap.entrySet().iterator()
	// is of this type. Each EntrySetEntry object provides an alternate, user-acceptable view of a DiskBackedMapEntry.
	private class EntrySetEntry implements Entry<K, V> {
		private DiskBackedMapEntry<K> entry;
		EntrySetEntry(DiskBackedMapEntry<K> entry) {
			this.entry = entry;
		}
		public boolean equals(Object o) {
			Entry<K, V> mo = (Entry<K, V>) o;
			Object thisValue = getValue();
			Object thisKey = getKey();
			return (mo.getKey() == null ?
					thisKey == null : mo.getKey().equals(thisKey))
					&&
					(mo.getValue() == null ?
							thisValue == null : mo.getValue().equals(thisValue));
		}
		public K getKey() {
			return entry.getKey();
		}
		public V getValue() {
			return DiskBackedMap.this.readValueNoError(entry);
		}
		public int hashCode() {
			V value = getValue();
			K key = getKey();
			return (key == null ? 0 : key.hashCode()) ^
					(value == null ? 0 : value.hashCode());
		}
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
	}

	// The actual entry set returned by DiskBackedMap.entrySet(). The values are demand-loaded. The iterator() and
	// toArray() methods ensure that the keys are returned in an order that causes sequential access to the values file.
	private class EntrySet extends AbstractSet<Entry<K, V>> {
		private EntrySet() {}
		public void clear() {
			throw new UnsupportedOperationException();
		}
		public boolean contains(Entry<K, V> o) {
			return DiskBackedMap.this.containsValue(o.getValue());
		}
		public Iterator<Entry<K, V>> iterator() {
			return new Iterator<Entry<K, V>>() {
				EntryIterator it = new EntryIterator();
				public Entry<K, V> next() {
					return new EntrySetEntry(it.next());
				}
				public boolean hasNext() {
					return it.hasNext();
				}
				public void remove() {
					it.remove();
				}
			};
		}
		public boolean equals(Object obj) {
			boolean eq = (this == obj);
			if(!eq) eq = super.equals(obj);
			return eq;
		}
		public int hashCode() {
			return super.hashCode();
		}
		public boolean remove(DiskBackedMapEntry<K> o) {
			throw new UnsupportedOperationException();
		}
		public boolean removeAll(Collection c) {
			throw new UnsupportedOperationException();
		}
		public boolean retainAll(Collection c) {
			throw new UnsupportedOperationException();
		}
		public int size() {
			return DiskBackedMap.this.size();
		}
	}

	// Specialized iterator for looping through the set returned by DiskBackedMap.keySet(). The iterator returns
	// the keys so that their corresponding DiskBackedMapEntry objects are sorted by file position value.
	private class KeyIterator implements Iterator<K> {
		private EntryIterator it;
		KeyIterator() {
			it = new EntryIterator();
		}
		public boolean hasNext() {
			return it.hasNext();
		}
		public K next() {
			return it.next().getKey();
		}
		public void remove() {
			it.remove();
		}
	}

	// Implements a key set -- the set of keys the caller used to store values in the DiskBackedMap. The iterator()
	// and toArray() methods ensure that the keys are returned in a manner that optimizes looping through
	// the associated values (as with the various iterators, above).
	private class KeySet extends AbstractSet<K> {
		ArrayList<K> keys = null;
		private KeySet() {}
		public void clear() {
			throw new UnsupportedOperationException();
		}
		public boolean contains(Object o) {
			return DiskBackedMap.this.indexMap.containsKey(o);
		}
		public boolean containsAll(Collection c) {
			boolean contains = true;
			Iterator it = c.iterator();
			while (contains && it.hasNext())
				contains = DiskBackedMap.this.indexMap.containsKey(it.next());
			return contains;
		}
		public boolean equals(Object o) {
			Set<K> so = (Set<K>) o;
			boolean eq = false;
			Set<K> myKeys = DiskBackedMap.this.indexMap.keySet();
			if(so.size() == myKeys.size()) {
				eq = true;
				Iterator<K> it = myKeys.iterator();
				while(eq) {
					K myKey = it.next();
					if(!so.contains(myKey)) eq = false;
				}
			}
			return eq;
		}
		public int hashCode() {
			return DiskBackedMap.this.indexMap.keySet().hashCode();
		}
		public boolean isEmpty() {
			return DiskBackedMap.this.indexMap.isEmpty();
		}
		public Iterator<K> iterator() {
			return new KeyIterator();
		}
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}
		public boolean removeAll(Collection c) {
			throw new UnsupportedOperationException();
		}
		public boolean retainAll(Collection c) {
			throw new UnsupportedOperationException();
		}
		public int size() {
			return DiskBackedMap.this.currentSize();
		}
		private synchronized void loadKeyArray() {
			if (keys == null) {
				List<DiskBackedMapEntry<K>> entries;
				entries = DiskBackedMap.this.getSortedEntries();
				keys = new ArrayList<>();
				keys.addAll(entries.stream().map(DiskBackedMapEntry::getKey).collect(Collectors.toList()));
			}
		}
	}

	/**
	 * Private instance data
	 */

	private HashMap<K, DiskBackedMapEntry<K>> indexMap = null;
	private File indexFilePath = null;
	private File valuesDBPath = null;
	private ValuesFile valuesDB = null;
	private boolean modified = false;
	private boolean valid = true;
	private EntrySet entrySetResult = null;
	private TreeSet<DiskBackedMapEntry<K>> fileGaps = null;

	/**
	 * Constructor
	 */

	public DiskBackedMap(String pathPrefix) throws IOException, ClassNotFoundException, OperationNotSupportedException {
		valuesDBPath = new File(pathPrefix + DATA_FILE_SUFFIX);
		indexFilePath = new File(pathPrefix + INDEX_FILE_SUFFIX);
		int filesFound = 0;
		if(valuesDBPath.exists())
			filesFound++;
		if(indexFilePath.exists())
			filesFound++;
		switch(filesFound) {
			case 0:
				createNewMap(this.valuesDBPath);
				break;
			case 1:
				String errorMessage = String.format(
						"One of the two required data files exists (%s or %s) but not the other\n",
							valuesDBPath.getName(), indexFilePath.getName());
				throw new FileNotFoundException(errorMessage);
			case 2:
				valuesDB = new ValuesFile(valuesDBPath);
				loadIndex();
				break;
			default:
				System.err.printf("Error, something weird happened. I'm throwing an operation not supported...\n");
				throw new OperationNotSupportedException();
		}
		findFileGaps();
	}

	protected void finalize() throws Throwable {
		try {
			close();
		} catch (IOException ex) {
			System.err.printf("Error during finalize: %s\n", ex);
		}
		super.finalize();
	}

	/**
	 * Public methods
	 */

	// Removes all mappings from this map. The data file is cleared by closing it, deleting it, and reopening it.
	// If an I/O error occurs at any point, this object will be closed and marked invalid.
	public synchronized void clear() {
		checkValidity();
		indexMap.clear();
		try {
			valuesDB.getFile().getChannel().truncate(0);
			modified = true;
		} catch (IOException ex) {
			System.err.printf("Failed to truncate DiskBackedMap file %s. Exception: %s\n", valuesDBPath.getPath(), ex);
			valid = false;
		}
	}


	// Close this map and saves the index to disk.
	public synchronized void close() throws IOException {
		if(valid) {
			save();
			valid = false;
		}
	}

	// Returns true if this map contains a mapping for the specified key. Since the keys are cached
	// in an in-memory index, this method does not have to access the data file, and is fairly cheap.
	public boolean containsKey(Object key) {
		checkValidity();
		return indexMap.containsKey(key);
	}

	// Returns true if this map maps one or more keys that are mapped to the specified value. Because
	// it must iterate through some portion of the on-disk value set, this method can be slow.
	public boolean containsValue(Object value) {
		checkValidity();
		return new ValueSet().contains(value);
	}

	// Deletes the files backing this DiskBackedMap. This method implicitly calls close()
	public void delete() {
		try {
			close();
		} catch (IOException ignored) {}
		deleteMapFiles();
	}

	// Calling this method on an invalid or closed DiskBackedMap will result in an exception.
	public Set<Entry<K, V>> entrySet() {
		checkValidity();
		if (entrySetResult == null)
			entrySetResult = new EntrySet();
		return entrySetResult;
	}

	// Compares the specified object with this map for equality. **Warning: Because this method
	// must compare the in-memory, and in-file values, this method can be quite slow.
	public boolean equals(Object o) {
		checkValidity();
		return super.equals(o);
	}

	// Returns the value associated with the the specified key or null if there is not mapping for this key.
	public V get(Object key) {
		checkValidity();
		V result = null;
		DiskBackedMapEntry<K> entry = indexMap.get(key);
		if (entry != null)
			result = readValueNoError(entry);
		return result;
	}

	// Retuns the hashcode value for this map.
	public int hashCode() {
		checkValidity();
		return super.hashCode();
	}

	// Once a DiskBackedMap has been closed, it is invalid and can no longer be used.
	public boolean isValid() {
		return valid;
	}

	// Returns a Set containing all the keys in this map. Since the keys are cached in
	// memory, this method is relatively efficient. The keys are returned in an order that
	// optimizes sequential access to the data file.
	public Set<K> keySet() {
		checkValidity();
		return new KeySet();
	}

	// Associates the specified value with the specified key in this map. If the map previously
	// contained a mapping for this key, the old value is replaced. This map does not permit nulls.
	public V put(K key, V value) throws ClassCastException, IllegalArgumentException, NullPointerException {
		checkValidity();
		V result = null;
		if (key == null)
			throw new NullPointerException("null key parameter");
		if(!(key instanceof Serializable))
			throw new IllegalArgumentException("Key is not serializable.");
		if (value == null)
			throw new NullPointerException("null value parameter");
		if (!(value instanceof Serializable))
			throw new IllegalArgumentException("Value is not serializable.");
		try {
			DiskBackedMapEntry<K> old = indexMap.get(key);
			// Read the old value then, modify index and write new value. (we can reclaim the old value's space)
			if (old != null) {
				result = readValueNoError(old);
				remove(key); // forces space to be listed in the gaps list
			}
			DiskBackedMapEntry<K> entry = writeValue(key, value);
			indexMap.put(key, entry);
			modified = true;
		} catch (IOException ex) {
			throw new IllegalArgumentException("Error saving value: " + ex.getMessage());
		}
		return result;
	}

	// Removes the mapping for this key from this map, if present.
	// Note: The space occupied by the serialized value in the data file is not combined at this point.
	public V remove(Object key) {
		checkValidity();
		V result = null;
		// We do nothing with the space in the data file for any existing item.
		// It remains in the data file, but is unreferenced.
		if (indexMap.containsKey(key)) {
			DiskBackedMapEntry<K> entry = indexMap.get(key);
			result = readValueNoError(entry);
			indexMap.remove(key);
			modified = true;
			findFileGaps();
		}
		return result;
	}


	// Save any in-memory index changes to disk without closing the map.
	public void save() throws IOException {
		checkValidity();
		if(modified) saveIndex();
	}

	// Returns the number of key-value mappings in this map. If the map contains more than Integer.MAX_VALUE
	// elements, returns Integer.MAX_VALUE. Queries the in-memory key index, so it is relatively efficient.
	public int size() {
		checkValidity();
		return currentSize();
	}

	// Returns a "thin "collection view of the values contained in this map. The collection contains
	// proxies for the actual disk-resident values; the values themselves are "lazily loaded".
	//
	// The collection is backed by the map, so changes to the map are reflected in the set. **Warning
	// the toArray() methods can be dangerous, since they will attempt to load every value from the
	// data file into an in-memory array.
	public Collection<V> values() {
		checkValidity();
		return new ValueSet();
	}

	/**
	 * Private methods
	 */

	// Throw an exception if the object isn't valid.
	private void checkValidity() {
		if(!valid) throw new IllegalStateException("Invalid DiskBackedMap object");
	}

	// Initialize a new index and data file for a hash map being created. (Only used by constructors)
	private void createNewMap(File valuesDBPath) throws IOException {
		this.valuesDB = new ValuesFile(valuesDBPath);
		//this.indexMap = new HashMap<>();
		this.indexMap = new HashMap<>();
	}

	// Determine the size of this map. (basically just consolidates the size-determination logic in one place)
	private int currentSize() {
		return indexMap.size();
	}

	// Locate gaps in the file by traversing the index. Initializes or re-initializes the fileGaps instance variable.
	private void findFileGaps() {
		if (fileGaps == null)
			fileGaps = new TreeSet<>(new DiskBackedMapEntryGapComparator()); //TreeSet<>(new DiskBackedMapEntryGapComparator());
		else
			fileGaps.clear();
		if (currentSize() > 0) {
			List<DiskBackedMapEntry<K>> entries = getSortedEntries();
			DiskBackedMapEntry<K> previous;
			Iterator<DiskBackedMapEntry<K>> it = entries.iterator();
			// Handle the first one specially.
			DiskBackedMapEntry<K> entry = it.next();
			long pos = entry.getFilePosition();
			int size = entry.getObjectSize();
			if (pos > 0) { // There's a gap at the beginning.
				size = (int) pos;
				fileGaps.add(new DiskBackedMapEntry<>((long) 0, size));
			}
			previous = entry;
			while (it.hasNext()) {
				entry = it.next();
				long previousPos = previous.getFilePosition();
				long possibleGapPos = previousPos + previous.getObjectSize();
				pos = entry.getFilePosition();
				assert (pos > previousPos);
				if (possibleGapPos != pos) {
					int gapSize = (int) (pos - possibleGapPos);
					fileGaps.add(new DiskBackedMapEntry<>(possibleGapPos, gapSize));
				}
				previous = entry;
			}
		}
	}

	// Get the list of DiskBackedMapEntry pointers for the values, sorted in ascending file position order.
	private List<DiskBackedMapEntry<K>> getSortedEntries() {
		List<DiskBackedMapEntry<K>> vals = new ArrayList<>();
		vals.addAll(indexMap.values());
		Collections.sort(vals, new DiskBackedMapEntryComparator());
		return vals;
	}

	// Load the index from its disk file.
	private void loadIndex() throws IOException, ClassNotFoundException {
		ObjectInputStream objStream;
		String version;
		objStream = new ObjectInputStream(new FileInputStream(this.indexFilePath));
		version = (String) objStream.readObject();
		if (!version.equals(VERSION_STAMP)) {
			String errorMessage = String.format("Version mismatch in %s. Expected version %s, found version %s.\n",
					indexFilePath.getName(), VERSION_STAMP, version);
			throw new IOException(errorMessage);
		}
		indexMap = (HashMap<K, DiskBackedMapEntry<K>>) objStream.readObject();
	}

	// Read an object from a specific location in the random access file data file.
	private V readValue(DiskBackedMapEntry<K> entry) throws IOException, ClassNotFoundException, IllegalStateException {
		int size = entry.getObjectSize();
		byte byteBuf[] = new byte[size];
		int sizeRead;
		ObjectInputStream objStream;
		synchronized (this) { // Load serialized object into memory.
			RandomAccessFile valuesFile = valuesDB.getFile();
			valuesFile.seek(entry.getFilePosition());
			if ((sizeRead = valuesFile.read(byteBuf)) != size) {
				String errorMessage = String.format(
					"Expected to read %d-bytes, only found %d-bytes from serialized obj in data file.\n",
						size, sizeRead);
				throw new IOException(errorMessage);
			}
		}
		objStream = new ObjectInputStream(new ByteArrayInputStream(byteBuf));
		return (V) objStream.readObject();
	}

	// Read an object without throwing a checked exception on error.
	private V readValueNoError(DiskBackedMapEntry<K> entry) {
		V obj = null;
		try {
			obj = readValue(entry);
		} catch (IOException | ClassNotFoundException ignored) {}
		return obj;
	}

	// Save the index to its disk file.
	private synchronized void saveIndex() throws IOException {
		ObjectOutputStream objStream = new ObjectOutputStream (new FileOutputStream(this.indexFilePath));
		objStream.writeObject(VERSION_STAMP);
		objStream.writeObject(indexMap);
	}

	 // Write an object to the end of the data file, recording its position and length in DiskBackedMapEntry object.
	 // Note: The object to be stored must implement the Serializable interface.
	private synchronized DiskBackedMapEntry<K> writeValue(K key, V obj) throws IOException {
		ObjectOutputStream objStream;
		ByteArrayOutputStream byteStream;
		int size;
		long filePos = -1;
		// Serialize the object to a byte buffer.
		byteStream = new ByteArrayOutputStream();
		objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(obj);
		size = byteStream.size();
		// Find a location for the object.
		filePos = findBestFitGap(size);
		RandomAccessFile valuesFile = this.valuesDB.getFile();
		if (filePos == -1)
			filePos = valuesFile.length();
		valuesFile.seek(filePos);
		valuesFile.write(byteStream.toByteArray()); // write bytes of serialized object
		return new DiskBackedMapEntry<>(filePos, size, key); // return entry
	}

	// Finds the smallest gap that can hold a serialized object.
	private long findBestFitGap(int objectSize) {
		long result = -1;
		assert (fileGaps != null);
		for(Iterator<DiskBackedMapEntry<K>> it = fileGaps.iterator(); it.hasNext(); ) {
			DiskBackedMapEntry<K> gap = it.next();
			long pos = gap.getFilePosition();
			int size = gap.getObjectSize();
			if (size >= objectSize) {
				result = pos;
				if (size > objectSize) {
					// Remove it and re-insert it, since the gap list is sorted by size.
					it.remove();
					pos += objectSize;
					size -= objectSize;
					gap.setFilePosition(pos);
					gap.setObjectSize(size);
					fileGaps.add(gap);
				}
				break;
			}
		}
		return result;
	}

	// Delete saved files on disk
	private void deleteMapFiles() {
		if (valuesDBPath != null) {
			valuesDBPath.delete();
			valuesDBPath = null;
		}
		if (indexFilePath != null) {
			indexFilePath.delete();
			indexFilePath = null;
		}
	}

}
