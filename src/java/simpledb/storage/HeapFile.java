package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
    
	private File f;
    private TupleDesc td;
	private int tid;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
		this.td = td;
		tid = f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            byte[] data = new byte[BufferPool.getPageSize()];
            int offset = pid.getPageNumber() * BufferPool.getPageSize();
            raf.seek(offset);
            raf.readFully(data);
            raf.close();
            return new HeapPage((HeapPageId) pid, data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to read page from file.");
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int) Math.ceil((double) f.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new DbFileIterator() {
            private int currentPage = -1;
            private Iterator<Tuple> currentIterator = null;
    
            @Override
            public void open() throws DbException, TransactionAbortedException {
                currentPage = 0;
                currentIterator = getPageTuples(currentPage);
            }
    
            private Iterator<Tuple> getPageTuples(int pageNo) throws TransactionAbortedException, DbException {
                PageId pid = new HeapPageId(getId(), pageNo);
                Page page = Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
                return ((HeapPage) page).iterator();
            }
    
            @Override
            public boolean hasNext() throws DbException, TransactionAbortedException {
                if (currentIterator == null) return false;
    
                if (currentIterator.hasNext()) return true;
    
                while (currentPage < numPages() - 1) {
                    currentPage++;
                    currentIterator = getPageTuples(currentPage);
                    if (currentIterator.hasNext()) return true;
                }
                return false;
            }
    
            @Override
            public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
                if (!hasNext()) throw new NoSuchElementException();
                return currentIterator.next();
            }
    
            @Override
            public void rewind() throws DbException, TransactionAbortedException {
                close();
                open();
            }
    
            @Override
            public void close() {
                currentPage = -1;
                currentIterator = null;
            }
        };
    }

}

