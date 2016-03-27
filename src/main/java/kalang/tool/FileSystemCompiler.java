package kalang.tool;

import kalang.util.ClassNameUtil;
import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import kalang.ast.ClassNode;
import kalang.compiler.Ast2Class;
import kalang.compiler.Ast2Java;
import kalang.compiler.AstLoader;
import kalang.compiler.CodeGenerator;
import kalang.compiler.CompilationUnit;
import kalang.compiler.CompileConfiguration;
import kalang.compiler.CompileConfigurationProxy;
import kalang.compiler.CompileError;
import kalang.compiler.CompileErrorHandler;
import kalang.compiler.DefaultCompileConfiguration;
import kalang.compiler.JavaAstLoader;
import kalang.compiler.KalangCompiler;
import org.apache.commons.io.FileUtils;

/**
 * The FileSystemCompiler compile sources from file system.
 * 
 * @author Kason Yang <i@kasonyang.com>
 */
public class FileSystemCompiler extends KalangCompiler implements CompileErrorHandler,CodeGenerator{

    private Map<String, File> sourceFiles = new HashMap<>();

    private List<URL> classPaths = new LinkedList<>();

    private File outputDir;

    public FileSystemCompiler() {
        this(new DefaultCompileConfiguration());
    }

    public FileSystemCompiler(CompileConfiguration config) {
        super();
        super.configuration =  new CompileConfigurationProxy(config){
            @Override
            public AstLoader getAstLoader() {
                URLClassLoader urlClassLoader = new URLClassLoader(classPaths.toArray(new URL[0]));
                JavaAstLoader astLoader = new JavaAstLoader(urlClassLoader);
                return astLoader;
            }

            @Override
            public CodeGenerator createCodeGenerator(CompilationUnit compilationUnit) {
                return codeGenerator;
            }
            
        };
        super.compileErrorHandler = this;
    }
    
    private CodeGenerator codeGenerator = this;

    public void addSource(File srcDir, File file) throws IOException {
        String className = ClassNameUtil.getClassName(srcDir, file);
        sourceFiles.put(className, file);
        super.addSource(className, FileUtils.readFileToString(file));
    }

    public void addSourceDir(File sourceDir) throws IOException {
        Collection<File> files = FileUtils.listFiles(sourceDir, new String[]{"kl"}, true);
        for (File f : files) {
            addSource(sourceDir , f);
        }
    }

    public void addClassPath(File path) {
        try {
            classPaths.add(path.toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FileSystemCompiler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void compile() {
        super.compile();
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void handleCompileError(CompileError error) {
        String cname = error.getCompilationUnit().getSource().getClassName();
        File fn = sourceFiles.get(cname);
        System.err.println(fn + ":" + error);
        setCompileTargetPhase(getCurrentCompilePhase());
    }
      
    private String generateJavaCode(ClassNode classNode){
        Ast2Java ast2Java = new Ast2Java();
        ast2Java.generate(classNode);
        return ast2Java.getCode();
    }
    
    private byte[] generateClassBytes(ClassNode clazz){
        Ast2Class ast2Class = new Ast2Class();
        ast2Class.generate(clazz);
        return ast2Class.getClassBytes();
    }

    @Override
    public void generate(ClassNode classNode) {
        String cls = classNode.name;
        if (outputDir != null) {
                String fname = cls.replace(".", "/");// + ".java";
                File destFile = new File(outputDir, fname + ".java");
                File classDest = new File(outputDir,fname + ".class");
                try{
                    FileUtils.write(destFile,generateJavaCode(classNode));
                    FileUtils.writeByteArrayToFile(classDest,generateClassBytes(classNode));
                }catch(IOException ex){
                    //TODO report io exception
                    System.err.println(ex.getMessage());
                }
            } else {
                System.out.println(generateJavaCode(classNode));
            }
    }

    public CodeGenerator getCodeGenerator() {
        return codeGenerator;
    }

    public void setCodeGenerator(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }
    
    

}
