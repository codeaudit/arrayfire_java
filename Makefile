AF_PATH ?= /opt/arrayfire
CUDA_PATH ?= /usr/local/cuda/
OCL_PATH ?= $(AF_PATH)

ifneq ($(shell uname), Linux)
$(error Only Linux supported for Java)
endif

ifeq ($(shell uname -m), x86_64)
  LIB:=lib64
else
  LIB:=lib
endif

AF_CFLAGS	= -I$(AF_PATH)/include
AF_CFLAGS      += -I$(JAVA_HOME)/include
AF_CFLAGS      += -I$(JAVA_HOME)/include/linux
AF_LIB_PATH	= $(AF_PATH)/$(LIB)/
AF_JAVA_PATH	= $(shell pwd)
AF_JAVA_LIB	= $(AF_JAVA_PATH)/$(LIB)/libaf_java.so
AF_JAVA_JAR	= $(AF_JAVA_PATH)/ArrayFire.jar
AF_JAVA_MANIFEST= $(AF_JAVA_PATH)/Manifest.txt
AF_JAVA_COM     = $(shell ls com/arrayfire/*.java)
AF_JAVA_CLASSES = $(patsubst %.java, %.class, $(AF_JAVA_COM))

ifeq ($(findstring opencl, $(MAKECMDGOALS)), opencl)
	AF_CFLAGS += -DAFCL -I$(OCL_PATH)/include
	AF=afcl
	AF_JAVA_LIB_EXT=$(patsubst %.so, %_ocl.so, $(AF_JAVA_LIB))
else
	AF_CFLAGS += -I$(CUDA_PATH)/include
	AF=afcu
	AF_JAVA_LIB_EXT=$(patsubst %.so, %_cuda.so, $(AF_JAVA_LIB))
endif

run: all
	AF_JAVA_PATH=$(AF_JAVA_PATH) make -C examples run

cuda: all

opencl: all

all: $(AF_JAVA_JAR)

$(AF_JAVA_JAR): $(AF_JAVA_LIB) $(AF_JAVA_CLASSES)
	jar cfm $@ $(AF_JAVA_MANIFEST) $(AF_JAVA_CLASSES)

%.class: %.java
	javac $<

$(AF_JAVA_LIB): $(AF_JAVA_LIB_EXT)
	cp $(AF_JAVA_LIB_EXT) $(AF_JAVA_LIB)
	cp $(AF_LIB_PATH)/lib$(AF).so $(AF_JAVA_PATH)/$(LIB)
ifeq ($(findstring opencl, $(MAKECMDGOALS)), opencl)
	cp $(AF_LIB_PATH)/libcl* $(AF_JAVA_PATH)/$(LIB)
endif

$(AF_JAVA_LIB_EXT): $(AF_JAVA_PATH)/src/java_wrapper.cpp
	gcc -shared -fPIC $< $(AF_CFLAGS) -L$(AF_LIB_PATH) -l$(AF) -o $@

clean:
	rm -f lib/*.so lib64/*.so $(AF_JAVA_JAR)
	rm -f $(AF_JAVA_CLASSES)
