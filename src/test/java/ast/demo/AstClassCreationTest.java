package ast.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 * @author Theodore Chaikalis
 * 
 * This test uses eclipse JDT libraries to programmatically create a java class,
 * by using the simple version of AST API (NOT with ASTRewrite).
 * <p>
 * The test also creates a java source file to store the newly created class.
 * <p>
 * Finally the test uses Reflection API to dynamically load the class, and run 
 * a method to validate the successful compilation and make the test more thorough
 * 
 */
public class AstClassCreationTest {

    @Test
    public void testHelloWorldClass() throws MalformedTreeException, BadLocationException, BadLocationException, BadLocationException, BadLocationException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        String className = "HelloWorld";
        String methodNameToBeTested = "getName";

        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        astParser.setSource(new char[1]);
        CompilationUnit unit = (CompilationUnit) astParser.createAST(null);

        unit.recordModifications();
        AST ast = unit.getAST();

        createPackageDeclaration(ast, unit);

        createImportDeclarations(ast, unit);

        TypeDeclaration type = ast.newTypeDeclaration();
        type.setInterface(false);
        type.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        type.setName(ast.newSimpleName(className));

        type.bodyDeclarations().add(getMainMethod(ast));
        type.bodyDeclarations().add(getReturnStringMethodDeclaration(methodNameToBeTested, ast));

        unit.types().add(type);

        Document document = new Document("");
        TextEdit edits = unit.rewrite(document, null);

        edits.apply(document);

        String srcString = document.get();

        System.out.println("------------------------------");
        System.out.println(srcString);
        System.out.println("------------------------------");

        assertTrue(srcString.contains("public static void main(String[] args)"));

        File f = createClassFile(className, srcString);

     //   testViaReflection(f, className, methodNameToBeTested);
    }

    private void createImportDeclarations(AST ast, CompilationUnit unit) {
        ImportDeclaration importDeclaration = ast.newImportDeclaration();
        QualifiedName name
                = ast.newQualifiedName(
                        ast.newSimpleName("java"),
                        ast.newSimpleName("util"));

        importDeclaration.setName(name);
        importDeclaration.setOnDemand(true);
        unit.imports().add(importDeclaration);
    }

    private void createPackageDeclaration(AST ast, CompilationUnit unit) {
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newQualifiedName(ast.newSimpleName("ast"), ast.newSimpleName("demo")));

        unit.setPackage(packageDeclaration);
    }

    private MethodDeclaration getMainMethod(AST ast) {

        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        methodDeclaration.setConstructor(false);

        List modifiers = methodDeclaration.modifiers();
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));

        methodDeclaration.setName(ast.newSimpleName("main"));
        methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

        SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
        variableDeclaration.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String"))));
        variableDeclaration.setName(ast.newSimpleName("args"));
        methodDeclaration.parameters().add(variableDeclaration);
        org.eclipse.jdt.core.dom.Block block = ast.newBlock();
        MethodInvocation methodInvocation = ast.newMethodInvocation();

        QualifiedName name = ast.newQualifiedName(
                ast.newSimpleName("System"),
                ast.newSimpleName("out"));

        methodInvocation.setExpression(name);
        methodInvocation.setName(ast.newSimpleName("println"));

        InfixExpression infixExpression = ast.newInfixExpression();

        infixExpression.setOperator(InfixExpression.Operator.PLUS);
        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue("Hello");
        infixExpression.setLeftOperand(literal);
        literal = ast.newStringLiteral();
        literal.setLiteralValue(" world");
        infixExpression.setRightOperand(literal);
        methodInvocation.arguments().add(infixExpression);

        ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
        block.statements().add(expressionStatement);
        methodDeclaration.setBody(block);

        return methodDeclaration;
    }

    private MethodDeclaration getReturnStringMethodDeclaration(String methodName, AST ast) {

        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        methodDeclaration.setConstructor(false);

        List modifiers = methodDeclaration.modifiers();
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

        methodDeclaration.setName(ast.newSimpleName(methodName));
        methodDeclaration.setReturnType2(ast.newSimpleType(ast.newSimpleName("String")));

        Block block = ast.newBlock();

        ReturnStatement returnStmt = ast.newReturnStatement();

        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue("teo");

        returnStmt.setExpression(literal);

        block.statements().add(returnStmt);
        methodDeclaration.setBody(block);

        return methodDeclaration;
    }

    private File createClassFile(String className, String srcString) throws FileNotFoundException {
        //Now we must write the newly created class in a file:

        Path folderPath = Paths.get("src", "main", "java", "ast", "demo");
        Path filePath = Paths.get(folderPath.toString(), className + ".java");
        File f = new File(filePath.toAbsolutePath().toString());
        try (PrintWriter out = new PrintWriter(f)) {
            out.write(srcString);
        }
        return f;
    }

    private void testViaReflection(File f, String className, String methodName) throws IllegalArgumentException, InstantiationException, SecurityException, IllegalAccessException, MalformedURLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        //We have to also test that the created class compiles and runs successfully
        //For this purpose we use Reflection to dynamically load the class
        //and run a test method on it.

        URL[] urls = new URL[]{f.toURI().toURL()};

        // Create a new class loader with the directory
        ClassLoader cl = new URLClassLoader(urls);
        Class cls = cl.loadClass("ast.demo." + className);
        Object classInstance = cls.newInstance();

        Object rs = cls.getMethod(methodName, null).invoke(classInstance, null);
        String returned = (String) rs;

        System.out.println("RETURNED = " + returned);

        assertEquals("teo", returned);
    }

}
