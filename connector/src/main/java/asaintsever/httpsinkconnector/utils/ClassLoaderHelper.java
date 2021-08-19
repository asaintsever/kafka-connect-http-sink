package asaintsever.httpsinkconnector.utils;

public class ClassLoaderHelper {

    private final ClassLoader classLoader;
    
    public static final ClassLoaderHelper INSTANCE = new ClassLoaderHelper();

    private ClassLoaderHelper() {
        classLoader = ClassLoaderHelper.class.getClassLoader();
    }

    
    public <T> T newInstance(Class<T> baseClass, String className) {
        Class<? extends T> foundClass = classForName(baseClass, className);
        return createInstance(foundClass);
    }
    
    public <T> Class<? extends T> classForName(Class<T> baseClass, String className) {
        try {
            return classForName(className).asSubclass(baseClass);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException("Could not find class " + className, e);
        }
    }
    
    public Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className, true, classLoader);
    }
    
    public <T> T createInstance(Class<T> toInstantiate) {
        if (toInstantiate == null) {
            throw new RuntimeException("Class to instantiate cannot be null or empty");
        }
        
        try {
            return toInstantiate.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find public constructor without arguments for class " + toInstantiate.getName(), e);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new RuntimeException("Could not instantiate class " + toInstantiate.getName(), e);
        }
    }
}
