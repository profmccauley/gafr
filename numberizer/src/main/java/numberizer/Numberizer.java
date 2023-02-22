package numberizer;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.*;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.io.FileInputStream;
import java.nio.file.Path;


public class Numberizer
{
  private static String prefix = "";
  private static HashSet<String> skips = new HashSet<>();

  private static class LineNumberStmt extends ExpressionStmt
  {
    public LineNumberStmt (Expression expression)
    {
      super(expression);
    }
  }

  /// Used as part of loop conditions so that we can set the line number before doing a condition expression which might throw
  private static Expression newConditional (int lineNum, Expression expr)
  {
    if (expr.isBooleanLiteralExpr()) return expr;
    BinaryExpr e = StaticJavaParser.parseExpression("GaFr.GFST.setLine(" + lineNum + ") || true").asBinaryExpr();
    e.setRight(expr);
    return e;
  }

  /// Used in never-executed else branches of special "if (true)" statements.
  private static class SafeThrowStmt extends ThrowStmt
  {
    public SafeThrowStmt ()
    {
      super( StaticJavaParser.parseExpression("new RuntimeException()") );
    }
  }


  private static class StatementCollector extends VoidVisitorAdapter<List<NodeList<Statement>>>
  {
    public void visit (BlockStmt s, List<NodeList<Statement>> r)
    {
      r.add(s.getStatements());
      super.visit(s, r);
    }
  }

  private static Statement newLNS (int linenum)
  {
    String lns = "GaFr.GFST.stLine = " + linenum + ";";
    ExpressionStmt s = StaticJavaParser.parseStatement(lns).asExpressionStmt();
    return new LineNumberStmt( s.getExpression() );
  }

  private static String getClassName (Statement body)
  {
    String className = "";
    Optional<Node> s = body.getParentNode();
    while (s.isPresent())
    {
      Node ss = s.get();
      if (ss instanceof ClassOrInterfaceDeclaration)
      {
        className = ((ClassOrInterfaceDeclaration)ss).getNameAsString() + "." + className;
        //break;
      }
      s = ss.getParentNode();
    }
    return className;
  }

  private static Statement fixStmt (Statement s)
  {
    if (s instanceof IfStmt)
    {
      IfStmt ss = (IfStmt)s;
      if (!ss.hasThenBlock())
      {
        BlockStmt b = new BlockStmt();
        int bodyLineNum = ss.getThenStmt().getBegin().get().line;
        b.addStatement(newLNS(bodyLineNum));
        b.addStatement(fixStmt(ss.getThenStmt())); // NWB only??
        ss.setThenStmt(b);
      }

      if (ss.hasElseBranch() && !ss.hasElseBlock())
      {
        Statement elseBranch = ss.getElseStmt().get();
        if (elseBranch instanceof IfStmt)
        {
          fixStmt(elseBranch);
        }
        else if (!(elseBranch instanceof SafeThrowStmt))
        {
          BlockStmt b = new BlockStmt();
          int bodyLineNum = elseBranch.getBegin().get().line;
          b.addStatement(newLNS(bodyLineNum));
          b.addStatement(fixStmt(elseBranch)); // NWB only
          ss.setElseStmt(b);
        }
      }
    }
    /*
    else if (s instanceof NodeWithBody)
    {
      int lineNum = s.getBegin().get().line;
      NodeWithBody ss = (NodeWithBody)s;
      Statement body = ss.getBody();
      if (!body.isBlockStmt())
      {
        int bodyLineNum = body.getBegin().get().line;
        ss.createBlockStatementAsBody()
          .addStatement(newLNS(bodyLineNum))
          .addStatement(fixStmt(body))
          .addStatement(newLNS(lineNum));
      }
      else
      {
        ((BlockStmt)body).addStatement(newLNS(lineNum));
      }
    }
    */
    else if (s instanceof ForStmt)
    {
      ForStmt ss = s.asForStmt();
      if (ss.getCompare().isPresent())
      {
        ss.setCompare( newConditional(s.getBegin().get().line, ss.getCompare().get()) );
      }
    }
    else if (s instanceof WhileStmt)
    {
      WhileStmt ss = s.asWhileStmt();
      ss.setCondition( newConditional(s.getBegin().get().line, ss.getCondition()) );
    }
    else if (s instanceof DoStmt)
    {
      DoStmt ss = s.asDoStmt();
      ss.setCondition( newConditional(s.getBegin().get().line, ss.getCondition()) );
    }
    else if (s.isSwitchStmt())
    {
      SwitchStmt ss = s.asSwitchStmt();
      for (SwitchEntry entry : ss.getEntries())
      {
        fixStatements(entry.getStatements());
      }
    }

    return s;
  }

  private static void fixStatements (NodeList<Statement> statements)
  {
    for (int i = 0; i < statements.size(); ++i)
    {
      Statement s = statements.get(i);
      if (s instanceof LineNumberStmt) continue;
      if (s instanceof ExplicitConstructorInvocationStmt) continue;

      int lineNum = s.getBegin().get().line;

      fixStmt(s);

      statements.add(i, newLNS(lineNum));
      ++i;
    }
  }

  public static void main (String[] args) throws Exception
  {
    String outDir = null, root = null;

    for (int i = 0; i < args.length; ++i)
    {
      if (args[i].startsWith("--prefix="))
      {
        prefix = args[i].split("=", 2)[1];
      }
      else if (args[i].startsWith("--skip="))
      {
        skips.add(args[i].split("=", 2)[1]);
      }
      else if (args[i].startsWith("--root="))
      {
        root = args[i].split("=", 2)[1];
      }
      else if (args[i].startsWith("--out="))
      {
        outDir = args[i].split("=", 2)[1];
      }
      else
      {
        continue;
      }
      args[i] = null;
    }

    if (root == null || outDir == null)
    {
      System.err.println("You must specify at least --root=... and --out=...");
      return;
    }

    SourceRoot sourceRoot = new SourceRoot(Path.of(root));

    for (int i = 0; i < args.length; ++i)
    {
      if (args[i] == null) continue;
      CompilationUnit cu = sourceRoot.parse("", args[i]);
      process(cu, args[i]);
    }

    sourceRoot.saveAll(Path.of(outDir));
  }

  public static void process (CompilationUnit cu, String fileName) throws Exception
  {
    // This skip mechanism isn't good; we should use annotations or magic
    // comments or something.
    //if (fileName.equals("GFST.java") || fileName.equals("GFU.java"))
    if (skips.contains(fileName))
    {
      System.err.println("Skipping " + fileName);
      return;
    }
    System.err.println("Adding line numbers to " + fileName);

    //CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(args[0]));

    List<NodeList<Statement>> allStatements = new ArrayList<>();
    new StatementCollector().visit(cu, allStatements);

    for (NodeList<Statement> statements : allStatements)
    {
      for (int i = 0; i < statements.size(); ++i)
      {
        if (!statements.get(i).isSwitchStmt()) continue;

        SwitchStmt s = statements.get(i).asSwitchStmt();
        IfStmt ifs = StaticJavaParser.parseStatement("if (true) true;").asIfStmt();
        ifs.setThenStmt(s);
        ifs.setElseStmt(new SafeThrowStmt() );
        statements.set(i, ifs);
      }

      fixStatements(statements);
    }

    // These next three sections are very similar and could probably be refactored.

    List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
    for (MethodDeclaration method : methods)
    {
      if (!method.getBody().isPresent()) continue;
      BlockStmt body = method.getBody().get();

      String className = getClassName(body);

      Statement methodName = StaticJavaParser.parseStatement("GaFr.GFST.pushStack(\"" + prefix + className + "" + method.getName() + "()\");");

      TryStmt tryStmt = StaticJavaParser.parseStatement("try { } catch (Throwable gafrThrow) { GaFr.GFST.doException(gafrThrow); throw gafrThrow; } finally { GaFr.GFST.popStack(); }").asTryStmt();
      tryStmt.setTryBlock(body);

      method.setBody(new BlockStmt(new NodeList<Statement>(methodName, tryStmt)));
    }

    for (InitializerDeclaration method : cu.findAll(InitializerDeclaration.class))
    {
      BlockStmt body = method.getBody();

      String className = getClassName(body);
      if (className.endsWith(".")) className = className.substring(0, className.length()-1);

      Statement methodName = StaticJavaParser.parseStatement("GaFr.GFST.pushStack(\"" + prefix + className + "\");");

      TryStmt tryStmt = StaticJavaParser.parseStatement("try { } catch (Throwable gafrThrow) { GaFr.GFST.doException(gafrThrow); throw gafrThrow; } finally { GaFr.GFST.popStack(); }").asTryStmt();
      tryStmt.setTryBlock(body);

      method.setBody(new BlockStmt(new NodeList<Statement>(methodName, tryStmt)));
    }

    List<ConstructorDeclaration> constructors = cu.findAll(ConstructorDeclaration.class);
    for (ConstructorDeclaration method : constructors)
    {
      BlockStmt body = method.getBody();

      String className = getClassName(body);

      NodeList<Statement> newbody = new NodeList<Statement>();

      NodeList<Statement> stmts = body.getStatements();
      if (stmts.size() > 0)
      {
        if (stmts.get(0) instanceof ExplicitConstructorInvocationStmt)
        {
          newbody.add( stmts.remove(0) );
        }
      }

      if (className.endsWith(".")) className = className.substring(0, className.length()-1);
      Statement methodName = StaticJavaParser.parseStatement("GaFr.GFST.pushStack(\"" + prefix + className + "\");");

      TryStmt tryStmt = StaticJavaParser.parseStatement("try { } catch (Throwable gafrThrow) { GaFr.GFST.doException(gafrThrow); throw gafrThrow; } finally { GaFr.GFST.popStack(); }").asTryStmt();
      tryStmt.setTryBlock(body);

      newbody.add(methodName);
      newbody.add(tryStmt);

      method.setBody(new BlockStmt(newbody));
    }

    //System.out.println(cu.toString());
    //sourceRoot.saveAll(Path.of(args[1]));
  }
}
