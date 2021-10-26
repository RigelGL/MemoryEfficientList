package rigellab;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.*;

public class MemoryEfficientList implements List {

    private static final byte TYPE_BOOLEAN = 0;
    private static final byte TYPE_BYTE = 1;
    private static final byte TYPE_SHORT = 2;
    private static final byte TYPE_CHAR = 3;
    private static final byte TYPE_INT = 4;
    private static final byte TYPE_FLOAT = 5;
    private static final byte TYPE_LONG = 6;
    private static final byte TYPE_DOUBLE = 7;


    private final Field[] fields;
    private final byte[] fieldTypes;
    private int size = 0;
    private ByteBuffer data = ByteBuffer.allocate(128);

    private int objectByteSize;
    private boolean buildWithDefaultValues;
    private Constructor<?> defaultConstructor;

    private Object firstFast;
    private Object secondFast;
    private Object fastInternal;
    private Object oldValue;


    public MemoryEfficientList(Class<?> clazz) {
        List<Field> correctFields = new ArrayList<>();
        for(Field f : clazz.getDeclaredFields()) {
            // not static
            if((f.getModifiers() & 8) == 0)
                correctFields.add(f);
        }

        fields = new Field[correctFields.size()];
        fieldTypes = new byte[correctFields.size()];

        for(int i = 0; i < fields.length; i++) {
            Field f = correctFields.get(i);
            fields[i] = f;
            f.setAccessible(true);
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


        for(Constructor<?> c : clazz.getDeclaredConstructors()) {
            if(c.getParameterCount() == 0) {
                defaultConstructor = c;
                buildWithDefaultValues = false;
                break;
            }
            else if(fields.length == c.getParameterCount()) {
                defaultConstructor = c;
                buildWithDefaultValues = true;
            }
        }

        if(defaultConstructor != null)
            defaultConstructor.setAccessible(true);

        try {
            firstFast = buildNewObject();
            secondFast = buildNewObject();
            fastInternal = buildNewObject();
            oldValue = buildNewObject();
        } catch(Exception ignored) {
            System.err.println("INVALID OBJECT CONSTRUCTOR");
        }
    }

    private Object getDefaultValue(byte type) {
        switch(type) {
            case TYPE_BOOLEAN:
                return false;
            case TYPE_BYTE:
                return (byte) 0;
            case TYPE_SHORT:
                return (short) 0;
            case TYPE_CHAR:
                return (char) 0;
            case TYPE_INT:
                return 0;
            case TYPE_FLOAT:
                return 0.0f;
            case TYPE_LONG:
                return 0L;
            case TYPE_DOUBLE:
                return 0.0;
        }
        return null;
    }

    private Object buildNewObject() throws Exception {
        if(buildWithDefaultValues) {
            switch(fields.length) {
                case 1:
                    return defaultConstructor.newInstance(getDefaultValue(fieldTypes[0]));
                case 2:
                    return defaultConstructor.newInstance(
                            getDefaultValue(fieldTypes[0]),
                            getDefaultValue(fieldTypes[1])
                    );
                case 3:
                    return defaultConstructor.newInstance(
                            getDefaultValue(fieldTypes[0]),
                            getDefaultValue(fieldTypes[1]),
                            getDefaultValue(fieldTypes[2])
                    );
                case 4:
                    return defaultConstructor.newInstance(
                            getDefaultValue(fieldTypes[0]),
                            getDefaultValue(fieldTypes[1]),
                            getDefaultValue(fieldTypes[2]),
                            getDefaultValue(fieldTypes[3])
                    );
                default:
                    return defaultConstructor.newInstance();
            }
        }
        else
            return defaultConstructor.newInstance();
    }

    private void composeObject(int pos, Object object) throws Exception {
        int offset = pos * objectByteSize;

        for(int i = 0; i < fields.length; i++) {
            switch(fieldTypes[i]) {
                case TYPE_BOOLEAN: {
                    fields[i].setBoolean(object, data.get(offset) == 1);
                    offset += 1;
                    break;
                }
                case TYPE_BYTE: {
                    fields[i].setByte(object, data.get(offset));
                    offset += 1;
                    break;
                }
                case TYPE_SHORT: {
                    fields[i].setShort(object, data.getShort(offset));
                    offset += 2;
                    break;
                }
                case TYPE_CHAR: {
                    fields[i].setChar(object, data.getChar(offset));
                    offset += 2;
                    break;
                }
                case TYPE_INT: {
                    fields[i].setInt(object, data.getInt(offset));
                    offset += 4;
                    break;
                }
                case TYPE_FLOAT: {
                    fields[i].setFloat(object, data.getFloat(offset));
                    offset += 4;
                    break;
                }
                case TYPE_LONG: {
                    fields[i].setLong(object, data.getLong(offset));
                    offset += 8;
                    break;
                }
                case TYPE_DOUBLE: {
                    fields[i].setDouble(object, data.getDouble(offset));
                    offset += 8;
                    break;
                }
            }
        }
    }

    private void decomposeObject(int pos, Object object) throws Exception {
        int offset = pos * objectByteSize;

        for(int i = 0; i < fields.length; i++) {
            Field f = fields[i];

            switch(fieldTypes[i]) {
                case TYPE_BOOLEAN: {
                    data.put(offset, (byte) (f.getBoolean(object) ? 1 : 0));
                    offset += 1;
                    break;
                }

                case TYPE_BYTE: {
                    data.put(offset, f.getByte(object));
                    offset += 1;
                    break;
                }

                case TYPE_SHORT: {
                    data.putShort(offset, f.getShort(object));
                    offset += 2;
                    break;
                }

                case TYPE_CHAR: {
                    data.putChar(offset, f.getChar(object));
                    offset += 2;
                    break;
                }

                case TYPE_INT: {
                    data.putInt(offset, f.getInt(object));
                    offset += 4;
                    break;
                }

                case TYPE_FLOAT: {
                    data.putFloat(offset, f.getFloat(object));
                    offset += 4;
                    break;
                }

                case TYPE_LONG: {
                    data.putLong(offset, f.getLong(object));
                    offset += 8;
                    break;
                }

                case TYPE_DOUBLE: {
                    data.putDouble(offset, f.getDouble(object));
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
        if((size + 1) * objectByteSize >= data.capacity()) {
            byte[] oldData = new byte[size * objectByteSize];
            data.position(size * objectByteSize);
            data.flip();
            data.get(oldData, 0, oldData.length);
            data = ByteBuffer.allocate(data.capacity() * 3 / 2 + 1);
            data.put(oldData);
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
            System.arraycopy(data, (index + 1) * objectByteSize,
                    data, index * objectByteSize, (newSize - index) * objectByteSize);

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
