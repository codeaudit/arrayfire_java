package com.arrayfire;

public class Array implements AutoCloseable {

    public static final int FloatType = 0;
    public static final int FloatComplexType = 1;
    public static final int DoubleType = 2;
    public static final int DoubleComplexType = 3;
    public static final int BooleanType = 4;
    public static final int IntType = 5;

    static {
        System.loadLibrary("af_java");
    }

    public native static void info();

    private native static long createEmptyArray(int[] dims, int type);
    private native static long createArrayFromFloat(int[] dims, float[] elems);
    private native static long createArrayFromDouble(int[] dims, double[] elems);
    private native static long createArrayFromFloatComplex(int[] dims, FloatComplex[] elems);
    private native static long createArrayFromDoubleComplex(int[] dims, DoubleComplex[] elems);
    private native static long createArrayFromInt(int[] dims, int[] elems);
    private native static long createArrayFromBoolean(int[] dims, boolean[] elems);

    private native static long createRanduArray(int[] dims, int type);
    private native static long createRandnArray(int[] dims, int type);
    private native static long createConstantsArray(double val, int[] dims, int type);


    private native static void destroyArray(long ref);
    private native static int[] getDims(long ref);
    private native static int   getType(long ref);

    private native static float  [] getFloatFromArray(long ref);
    private native static double [] getDoubleFromArray(long ref);
    private native static int    [] getIntFromArray(long ref);
    private native static boolean       [] getBooleanFromArray(long ref);
    private native static FloatComplex  [] getFloatComplexFromArray(long ref);
    private native static DoubleComplex [] getDoubleComplexFromArray(long ref);

    private native static long fft  (long a);
    private native static long fft2 (long a);
    private native static long fft3 (long a);

    private native static long ifft (long a);
    private native static long ifft2(long a);
    private native static long ifft3(long a);

    // Binary operations
    private native static long add(long a, long b);
    private native static long sub(long a, long b);
    private native static long mul(long a, long b);
    private native static long div(long a, long b);
    private native static long le (long a, long b);
    private native static long lt (long a, long b);
    private native static long ge (long a, long b);
    private native static long gt (long a, long b);
    private native static long eq (long a, long b);
    private native static long ne (long a, long b);

    // Unary operations
    private native static long sin  (long a);
    private native static long cos  (long a);
    private native static long tan  (long a);
    private native static long asin (long a);
    private native static long acos (long a);
    private native static long atan (long a);
    private native static long sinh (long a);
    private native static long cosh (long a);
    private native static long tanh (long a);
    private native static long asinh(long a);
    private native static long acosh(long a);
    private native static long atanh(long a);
    private native static long exp  (long a);
    private native static long log  (long a);
    private native static long abs  (long a);
    private native static long sqrt (long a);

    // Scalar return operations
    private native static double sumAll(long a);
    private native static double maxAll(long a);
    private native static double minAll(long a);

    private native static long sum(long a, int dim);
    private native static long max(long a, int dim);
    private native static long min(long a, int dim);

    // Scalar operations
    private native static long addf(long a, float b);
    private native static long subf(long a, float b);
    private native static long mulf(long a, float b);
    private native static long divf(long a, float b);
    private native static long lef (long a, float b);
    private native static long ltf (long a, float b);
    private native static long gef (long a, float b);
    private native static long gtf (long a, float b);
    private native static long eqf (long a, float b);
    private native static long nef (long a, float b);
    private native static long pow (long a, float b);
    private native static long fsub(float a, long b);
    private native static long fdiv(float a, long b);
    private native static long fle (float a, long b);
    private native static long flt (float a, long b);
    private native static long fge (float a, long b);
    private native static long fgt (float a, long b);

    // Global reference to JVM object
    // to persist between JNI calls
    long ref;

    public Array() {
        ref = 0;
    }

    public int[] dims() {
        return getDims(ref);
    }

    public int type() {
        return getType(ref);
    }

    public String typeName(int ty) throws Exception {
        if (ty == FloatType) return "float";
        if (ty == DoubleType) return "double";
        if (ty == IntType) return "int";
        if (ty == BooleanType) return "boolean";
        if (ty == FloatComplexType) return "FloatComplex";
        if (ty == DoubleComplexType) return "DoubleComplex";
        throw new Exception("Unknown type");
    }

    private static int[] dim4(int[] dims) throws Exception {

        if( dims == null ) {
            throw new Exception("Null dimensions object provided");
        } else if ( dims.length > 4 ) {
            throw new Exception("ArrayFire supports up to 4 dimensions only");
        }

        int[] adims;
        adims = new int[] {1, 1, 1, 1};
        for (int i = 0; i < dims.length; i++) adims[i] = dims[i];

        return adims;
    }

    private void assertType(int ty) throws Exception {

        int myType = type();

        if( myType != ty ) {
            String str = "Type mismatch: ";
            str = str + "Requested " + typeName(ty);
            str = str + ". Found " + typeName(myType);
            throw new Exception(str);
        }
        return;
    }

    // Below version of constructor
    // allocates space on device and initializes
    // all elemets to zero

    public Array(int[] dims, int type) throws Exception {
        int[] adims = dim4(dims);
        ref = createEmptyArray(adims, type);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public Array(int[] dims) throws Exception {
        int[] adims = dim4(dims);
        ref = createEmptyArray(adims, FloatType);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public Array(int[] dims, float[] elems) throws Exception {
        int[] adims = dim4(dims);

        int total_size = 1;
        for (int i = 0; i < adims.length; i++) total_size *= adims[i];

        if(elems == null) {
            throw new Exception("Null elems object provided");
        }

        if( elems.length > total_size || elems.length < total_size ) {
            throw new Exception("Mismatching dims and array size");
        }

        ref = createArrayFromFloat(adims, elems);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public Array(int[] dims, double[] elems) throws Exception {
        int[] adims = dim4(dims);

        int total_size = 1;
        for (int i = 0; i < adims.length; i++) total_size *= adims[i];

        if(elems == null) {
            throw new Exception("Null elems object provided");
        }

        if( elems.length > total_size || elems.length < total_size ) {
            throw new Exception("Mismatching dims and array size");
        }

        ref = createArrayFromDouble(adims, elems);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public Array(int[] dims, int[] elems) throws Exception {
        int[] adims = dim4(dims);

        int total_size = 1;
        for (int i = 0; i < adims.length; i++) total_size *= adims[i];

        if(elems == null) {
            throw new Exception("Null elems object provided");
        }

        if( elems.length > total_size || elems.length < total_size ) {
            throw new Exception("Mismatching dims and array size");
        }

        ref = createArrayFromInt(adims, elems);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public Array(int[] dims, FloatComplex[] elems) throws Exception {
        int[] adims = dim4(dims);

        int total_size = 1;
        for (int i = 0; i < adims.length; i++) total_size *= adims[i];

        if(elems == null) {
            throw new Exception("Null elems object provided");
        }

        if( elems.length > total_size || elems.length < total_size ) {
            throw new Exception("Mismatching dims and array size");
        }

        ref = createArrayFromFloatComplex(adims, elems);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public Array(int[] dims, DoubleComplex[] elems) throws Exception {
        int[] adims = dim4(dims);

        int total_size = 1;
        for (int i = 0; i < adims.length; i++) total_size *= adims[i];

        if(elems == null) {
            throw new Exception("Null elems object provided");
        }

        if( elems.length > total_size || elems.length < total_size ) {
            throw new Exception("Mismatching dims and array size");
        }

        ref = createArrayFromDoubleComplex(adims, elems);
        if (ref == 0) throw new Exception("Failed to create Array");
    }

    public float[] getFloatArray() throws Exception {
        assertType(FloatType);
        return getFloatFromArray(ref);
    }

    public double[] getDoubleArray() throws Exception {
        assertType(DoubleType);
        return getDoubleFromArray(ref);
    }

    public FloatComplex[] getFloatComplexArray() throws Exception {
        assertType(FloatComplexType);
        return getFloatComplexFromArray(ref);
    }

    public DoubleComplex[] getDoubleComplexArray() throws Exception {
        assertType(DoubleComplexType);
        return getDoubleComplexFromArray(ref);
    }

    public int[] getIntArray() throws Exception {
        assertType(IntType);
        return getIntFromArray(ref);
    }

    public boolean[] getBooleanArray() throws Exception {
        assertType(BooleanType);
        return getBooleanFromArray(ref);
    }

    // Binary operations

    public static Array randu(int[] dims, int type) throws Exception {
        int[] adims = dim4(dims);
        long ref = createRanduArray(adims, type);
        if (ref == 0) throw new Exception("Failed to create Array");

        Array ret_val = new Array();
        ret_val.ref = ref;
        return ret_val;
    }

    public static Array randn(int[] dims, int type) throws Exception {
        int[] adims = dim4(dims);
        long ref = createRandnArray(adims, type);
        if (ref == 0) throw new Exception("Failed to create Array");

        Array ret_val = new Array();
        ret_val.ref = ref;
        return ret_val;
    }

    public static Array constant(double val, int[] dims, int type) throws Exception {
        int[] adims = dim4(dims);
        long ref = createConstantsArray(val, adims, type);
        if (ref == 0) throw new Exception("Failed to create Array");

        Array ret_val = new Array();
        ret_val.ref = ref;
        return ret_val;
    }

    public static Array add(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = add(a.ref,b.ref);
        return ret_val;
    }

    public static Array sub(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = sub(a.ref,b.ref);
        return ret_val;
    }

    public static Array mul(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = mul(a.ref,b.ref);
        return ret_val;
    }

    public static Array div(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = div(a.ref,b.ref);
        return ret_val;
    }

    public static Array le(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = le(a.ref,b.ref);
        return ret_val;
    }

    public static Array lt(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = lt(a.ref,b.ref);
        return ret_val;
    }

    public static Array ge(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = ge(a.ref,b.ref);
        return ret_val;
    }

    public static Array gt(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = gt(a.ref,b.ref);
        return ret_val;
    }

    public static Array eq(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = eq(a.ref,b.ref);
        return ret_val;
    }

    public static Array ne(Array a, Array b) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = ne(a.ref,b.ref);
        return ret_val;
    }

    // Unary operations
    public static Array sin(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = sin(a.ref);
        return ret_val;
    }

    public static Array cos(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = cos(a.ref);
        return ret_val;
    }

    public static Array tan(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = tan(a.ref);
        return ret_val;
    }

    public static Array asin(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = asin(a.ref);
        return ret_val;
    }

    public static Array acos(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = acos(a.ref);
        return ret_val;
    }

    public static Array atan(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = atan(a.ref);
        return ret_val;
    }

    public static Array sinh(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = sinh(a.ref);
        return ret_val;
    }

    public static Array cosh(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = cosh(a.ref);
        return ret_val;
    }

    public static Array tanh(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = tanh(a.ref);
        return ret_val;
    }

    public static Array asinh(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = asinh(a.ref);
        return ret_val;
    }
    public static Array acosh(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = acosh(a.ref);
        return ret_val;
    }
    public static Array atanh(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = atanh(a.ref);
        return ret_val;
    }
    public static Array exp(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = exp(a.ref);
        return ret_val;
    }
    public static Array log(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = log(a.ref);
        return ret_val;
    }
    public static Array abs(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = abs(a.ref);
        return ret_val;
    }
    public static Array sqrt(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = sqrt(a.ref);
        return ret_val;
    }

    // Scalar return operations
    public static double sumAll(Array a) throws Exception { return sumAll(a.ref); }
    public static double maxAll(Array a) throws Exception { return maxAll(a.ref); }
    public static double minAll(Array a) throws Exception { return minAll(a.ref); }

    public static Array fft(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = fft(a.ref);
        return ret_val;
    }

    public static Array fft2(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = fft2(a.ref);
        return ret_val;
    }

    public static Array fft3(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = fft3(a.ref);
        return ret_val;
    }

    public static Array ifft(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = ifft(a.ref);
        return ret_val;
    }

    public static Array ifft2(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = ifft2(a.ref);
        return ret_val;
    }

    public static Array ifft3(Array a) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = ifft3(a.ref);
        return ret_val;
    }

    public static Array sum(Array a, int dim) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = sum(a.ref, dim);
        return ret_val;
    }

    public static Array max(Array a, int dim) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = max(a.ref, dim);
        return ret_val;
    }

    public static Array min(Array a, int dim) throws Exception {
        Array ret_val = new Array();
        ret_val.ref = min(a.ref, dim);
        return ret_val;
    }

    public static Array sum(Array a) throws Exception {
        return sum(a, -1);
    }

    public static Array max(Array a) throws Exception {
        return max(a, -1);
    }

    public static Array min(Array a) throws Exception {
        return min(a, -1);
    }

    // Scalar operations
    public static Array add(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = addf(a.ref,b);
        return res;
    }

    public static Array sub(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = subf(a.ref,b);
        return res;
    }

    public static Array mul(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = mulf(a.ref,b);
        return res;
    }

    public static Array div(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = divf(a.ref,b);
        return res;
    }

    public static Array le(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = lef(a.ref,b);
        return res;
    }

    public static Array lt(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = ltf(a.ref,b);
        return res;
    }

    public static Array ge(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = gef(a.ref,b);
        return res;
    }

    public static Array gt(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = gtf(a.ref,b);
        return res;
    }

    public static Array eq(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = eqf(a.ref,b);
        return res;
    }

    public static Array ne(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = nef(a.ref,b);
        return res;
    }

    public static Array pow(Array a, float b) throws Exception {
        Array res = new Array();
        res.ref = pow(a.ref,b);
        return res;
    }

    public static Array sub(float a, Array b) throws Exception {
        Array res = new Array();
        res.ref = fsub(a,b.ref);
        return res;
    }

    public static Array div(float a, Array b) throws Exception {
        Array res = new Array();
        res.ref = fdiv(a,b.ref);
        return res;
    }

    public static Array le(float a, Array b) throws Exception {
        Array res = new Array();
        res.ref = fle(a,b.ref);
        return res;
    }

    public static Array lt(float a, Array b) throws Exception {
        Array res = new Array();
        res.ref = flt(a,b.ref);
        return res;
    }

    public static Array ge(float a, Array b) throws Exception {
        Array res = new Array();
        res.ref = fge(a,b.ref);
        return res;
    }

    public static Array gt(float a, Array b) throws Exception {
        Array res = new Array();
        res.ref = fgt(a,b.ref);
        return res;
    }

    @Override
    public void close() throws Exception {
        if (ref != 0) destroyArray(ref);
    }

}
