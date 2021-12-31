package rigellab;

import java.lang.reflect.Field;
import java.util.*;
import sun.misc.Unsafe;

public class MemoryEfficientList implements List {

    private static final byte TYPE_BOOLEAN = 0;
    private static final byte TYPE_BYTE = 1;
    private static final byte TYPE_SHORT = 2;
    private static final byte TYPE_CHAR = 3;
    private static final byte TYPE_INT = 4;
    private static final byte TYPE_FLOAT = 5;
    private static final byte TYPE_LONG = 6;
    private static final byte TYPE_DOUBLE = 7;


    private Class clazz;
    private final byte[] fieldTypes;
    private final long[] fieldOffsets;
    private int size = 0;
    private int dataSize;
    private long dataAddress;

    private int objectByteSize;

    private Object firstFast;
    private Object secondFast;
    private Object fastInternal;
    private Object oldValue;

    private static final Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    public MemoryEfficientList(Class<?> clazz) {
        this.clazz = clazz;
        List<Field> correctFields = getCorrectFields(clazz);

        fieldTypes = new byte[correctFields.size()];
        fieldOffsets = new long[correctFields.size()];

        initFields(correctFields);

        dataSize = 16;
        dataAddress = unsafe.allocateMemory(objectByteSize * 16);

        try {
            firstFast = buildNewObject();
            secondFast = buildNewObject();
            fastInternal = buildNewObject();
            oldValue = buildNewObject();
        } catch(Exception ignored) {
        }
    }

    public MemoryEfficientList(Class<?> clazz, int size) {
        this.clazz = clazz;
        List<Field> correctFields = getCorrectFields(clazz);

        fieldTypes = new byte[correctFields.size()];
        fieldOffsets = new long[correctFields.size()];

        initFields(correctFields);

        dataSize = size;
        dataAddress = unsafe.allocateMemory(objectByteSize * size);

        try {
            firstFast = buildNewObject();
            secondFast = buildNewObject();
            fastInternal = buildNewObject();
            oldValue = buildNewObject();
        } catch(Exception ignored) {
        }
    }

    private List<Field> getCorrectFields(Class<?> clazz) {

        List<Field> correctFields = new ArrayList<>();
        for(Field f : clazz.getDeclaredFields()) {
            // not static
            if((f.getModifiers() & 8) == 0)
                correctFields.add(f);
        }
        return correctFields;
    }

    private void initFields(List<Field> correctFields) {
        for(int i = 0; i < fieldTypes.length; i++) {
            Field f = correctFields.get(i);
            fieldOffsets[i] = unsafe.objectFieldOffset(f);
            if(f.getType() == boolean.class) {
                fieldTypes[i] = TYPE_BOOLEAN;
                objectByteSize += 1;
            }
            else if(f.getType() == byte.class) {
                fieldTypes[i] = TYPE_BYTE;
                objectByteSize += 1;
            }
            else if(f.getType() == short.class) {
                fieldTypes[i] = TYPE_SHORT;
                objectByteSize += 2;
            }
            else if(f.getType() == char.class) {
                fieldTypes[i] = TYPE_CHAR;
                objectByteSize += 2;
            }
            else if(f.getType() == int.class) {
                fieldTypes[i] = TYPE_INT;
                objectByteSize += 4;
            }
            else if(f.getType() == float.class) {
                fieldTypes[i] = TYPE_FLOAT;
                objectByteSize += 4;
            }
            else if(f.getType() == long.class) {
                fieldTypes[i] = TYPE_LONG;
                objectByteSize += 8;
            }
            else if(f.getType() == double.class) {
                fieldTypes[i] = TYPE_DOUBLE;
                objectByteSize += 8;
            }
        }
    }


    private Object buildNewObject() throws Exception {
        return unsafe.allocateInstance(clazz);
    }

    private void composeObject(int pos, Object object) {
        long offset = pos * objectByteSize;

        for(int i = 0; i < fieldTypes.length; i++) {
            long fo = fieldOffsets[i];
            switch(fieldTypes[i]) {
                case TYPE_BOOLEAN: {
                    unsafe.putBoolean(object, fo, unsafe.getByte(dataAddress + offset) == 1);
                    offset += 1;
                    break;
                }
                case TYPE_BYTE: {
                    unsafe.putByte(object, fo, unsafe.getByte(dataAddress + offset));
                    offset += 1;
                    break;
                }
                case TYPE_SHORT: {
                    unsafe.putShort(object, fo, unsafe.getShort(dataAddress + offset));
                    offset += 2;
                    break;
                }
                case TYPE_CHAR: {
                    unsafe.putChar(object, fo, unsafe.getChar(dataAddress + offset));
                    offset += 2;
                    break;
                }
                case TYPE_INT: {
                    unsafe.putInt(object, fo, unsafe.getInt(dataAddress + offset));
                    offset += 4;
                    break;
                }
                case TYPE_FLOAT: {
                    unsafe.putFloat(object, fo, unsafe.getFloat(dataAddress + offset));
                    offset += 4;
                    break;
                }
                case TYPE_LONG: {
                    unsafe.putLong(object, fo, unsafe.getLong(dataAddress + offset));
                    offset += 8;
                    break;
                }
                case TYPE_DOUBLE: {
                    unsafe.putDouble(object, fo, unsafe.getDouble(dataAddress + offset));
                    offset += 8;
                    break;
                }
            }
        }
    }

    private void decomposeObject(int pos, Object object) {
        int offset = pos * objectByteSize;

        for(int i = 0; i < fieldTypes.length; i++) {
            long fo = fieldOffsets[i];

            switch(fieldTypes[i]) {
                case TYPE_BOOLEAN: {
                    unsafe.putByte(dataAddress + offset, (byte) (unsafe.getBoolean(object, fo) ? 1 : 0));
                    offset += 1;
                    break;
                }

                case TYPE_BYTE: {
                    unsafe.putByte(dataAddress + offset, unsafe.getByte(object, fo));
                    offset += 1;
                    break;
                }

                case TYPE_SHORT: {
                    unsafe.putShort(dataAddress + offset, unsafe.getShort(object, fo));
                    offset += 2;
                    break;
                }

                case TYPE_CHAR: {
                    unsafe.putChar(dataAddress + offset, unsafe.getChar(object, fo));
                    offset += 2;
                    break;
                }

                case TYPE_INT: {
                    unsafe.putInt(dataAddress + offset, unsafe.getInt(object, fo));
                    offset += 4;
                    break;
                }

                case TYPE_FLOAT: {
                    unsafe.putFloat(dataAddress + offset, unsafe.getFloat(object, fo));
                    offset += 4;
                    break;
                }

                case TYPE_LONG: {
                    unsafe.putLong(dataAddress + offset, unsafe.getLong(object, fo));
                    offset += 8;
                    break;
                }

                case TYPE_DOUBLE: {
                    unsafe.putDouble(dataAddress + offset, unsafe.getDouble(object, fo));
                    offset += 8;
                    break;
                }
            }
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        try {
            for(int i = 0; i < size; i++) {
                decomposeObject(i, fastInternal);
                if(Objects.equals(fastInternal, 0))
                    return true;
            }
        } catch(Exception ignored) {
        }
        return false;
    }

    @Override
    public Iterator iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        try {
            for(int i = 0; i < size; i++) {
                arr[i] = buildNewObject();
                composeObject(i, arr[i]);
            }
        } catch(Exception ignored) {
        }
        return arr;
    }

    @Override
    public Object[] toArray(Object[] a) {
        try {
            final int sz = Math.min(size, a.length);
            for(int i = 0; i < sz; i++) {
                if(a[i] == null)
                    a[i] = buildNewObject();
                composeObject(i, a[i]);
            }
        } catch(Exception ignored) {

        }
        return a;
    }

    @Override
    public boolean add(Object o) {
        if(size >= dataSize) {
            dataSize = dataSize * 3 / 2 + 1;
            long newAddress = unsafe.allocateMemory(dataSize * objectByteSize);
            unsafe.copyMemory(dataAddress, newAddress, size * objectByteSize);
            unsafe.freeMemory(dataAddress);
            dataAddress = newAddress;
        }

        try {
            decomposeObject(size, o);
            size += 1;
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection c) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }


    public void clear() {
        size = 0;
    }

    @Override
    public Object get(int index) {
        if(index < 0 || index >= size)
            throw new ArrayIndexOutOfBoundsException(index);

        try {
            Object result = buildNewObject();
            composeObject(index, result);
            return result;
        } catch(Exception e) {
            return null;
        }
    }

    public Object getFast(int index) {
        if(index < 0 || index >= size)
            throw new ArrayIndexOutOfBoundsException(index);

        try {
            Object result = firstFast;
            composeObject(index, result);
            return result;
        } catch(Exception e) {
            return null;
        }
    }

    public Object getFast2(int index) {
        if(index < 0 || index >= size)
            throw new ArrayIndexOutOfBoundsException(index);

        try {
            Object result = secondFast;
            composeObject(index, result);
            return result;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public Object set(int index, Object element) {
        if(index < 0 || index >= size)
            throw new ArrayIndexOutOfBoundsException(index);

        try {
            composeObject(index, oldValue);
            decomposeObject(index, element);
        } catch(Exception ignored) {

        }

        return oldValue;
    }

    @Override
    public void add(int index, Object element) {

    }

    @Override
    public Object remove(int index) {
        if(index < 0 || index >= size)
            throw new ArrayIndexOutOfBoundsException(index);
        try {
            composeObject(index, oldValue);
        } catch(Exception ignored) {
        }

        final int newSize = size - 1;

        if(newSize > index)
            unsafe.copyMemory((index + 1) * objectByteSize, index * objectByteSize, (newSize - index) * objectByteSize);

        size = newSize;
        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator listIterator() {
        return new ListItr(0);
    }

    @Override
    public ListIterator listIterator(int index) {
        if(index < 0 || index >= size)
            throw new IndexOutOfBoundsException(index);
        return new ListItr(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return null;
    }

    public int hashCode() {
        int hashCode = 1;
        Object e = fastInternal;
        try {
            for(int i = 0; i < size; i++) {
                composeObject(i, e);
                hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
            }
        } catch(Exception ignored) {
        }
        return hashCode;
    }

    public String toString() {
        Iterator it = iterator();
        if(!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for(; ; ) {
            Object e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if(!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    private class Itr implements Iterator {
        int cursor = 0;
        int lastRet = -1;
        Object fastIterator = null;

        Itr() {
            try {
                fastIterator = buildNewObject();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public Object next() {
            int i = cursor;
            if(i >= size)
                throw new NoSuchElementException();
            cursor = i + 1;
            lastRet = i;
            try {
                composeObject(lastRet, fastIterator);
            } catch(Exception ignored) {
            }

            return fastIterator;
        }

        @Override
        public void remove() {
            if(lastRet < 0)
                throw new IllegalStateException();

            try {
                MemoryEfficientList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
            } catch(IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class ListItr extends Itr implements ListIterator {

        ListItr(int index) {
            super();
            cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public Object previous() {
            int i = cursor - 1;
            if(i < 0)
                throw new NoSuchElementException();
            cursor = i;
            lastRet = i;
            try {
                composeObject(lastRet, fastIterator);
            } catch(Exception ignored) {
            }
            return fastIterator;
        }

        @Override
        public void set(Object o) {
            if(lastRet < 0)
                throw new IllegalStateException();

            try {
                MemoryEfficientList.this.set(lastRet, o);
            } catch(IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void add(Object o) {
            try {
                int i = cursor;
                MemoryEfficientList.this.add(i, o);
                cursor = i + 1;
                lastRet = -1;
            } catch(IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

}
