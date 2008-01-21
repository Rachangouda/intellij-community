/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.GrClosureType;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author ilyas
 */
public class GrMethodCallExpressionImpl extends GrCallExpressionImpl implements GrMethodCallExpression {
  public GrMethodCallExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "Method call";
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitMethodCallExpression(this);
  }

  public PsiType getType() {
    GrExpression invoked = getInvokedExpression();
    if (invoked instanceof GrReferenceExpression) {
      GrReferenceExpression refExpr = (GrReferenceExpression) invoked;
      final GroovyResolveResult resolveResult = refExpr.advancedResolve();
      PsiElement resolved = resolveResult.getElement();
      PsiType returnType = null;
      if (resolved instanceof PsiMethod && !GroovyPsiManager.getInstance(resolved.getProject()).isTypeBeingInferred(resolved)) {
        returnType = ((PsiMethod) resolved).getReturnType();
      } else if (resolved instanceof GrVariable) {
        final PsiType type = ((GrVariable) resolved).getTypeGroovy();
        if (type instanceof GrClosureType) {
          returnType = ((GrClosureType) type).getClosureReturnType();
        }
      }
      if (returnType != null) {
        returnType = resolveResult.getSubstitutor().substitute(returnType);
        returnType = TypesUtil.boxPrimitiveType(returnType, getManager(), getResolveScope());
      }
      if (refExpr.getDotTokenType() != GroovyTokenTypes.mSPREAD_DOT) {
        return returnType;
      } else {
        return ResolveUtil.getListTypeForSpreadOperator(refExpr, returnType);
      }
    }

    return null;
  }

  public GrExpression getInvokedExpression() {
    return findChildByClass(GrExpression.class);
  }

  public GrExpression replaceClosureArgument(@NotNull GrClosableBlock closure, @NotNull GrExpression newExpr) throws IncorrectOperationException {

    ASTNode parentNode = this.getParent().getNode();
    if (!(newExpr instanceof GrClosableBlock)) {
      ArrayList<GrExpression> allArgs = new ArrayList<GrExpression>();
      // Collecting all arguments
      allArgs.addAll(Arrays.asList(getExpressionArguments()));
      ArrayList<GrExpression> closureArgs = new ArrayList<GrExpression>();
      for (GrExpression closArg : getClosureArguments()) {
        if (closArg.equals(closure)) break;
        closureArgs.add(closArg);
      }
      allArgs.addAll(closureArgs);
      allArgs.add(newExpr);
      int refIndex = allArgs.size() - 1;

      // New argument list
      GrArgumentList newArgList = GroovyPsiElementFactory.getInstance(getProject()).createExpressionArgumentList(allArgs.toArray(GrExpression.EMPTY_ARRAY));
      while (closure.getNode().getTreePrev() != null &&
          !(closure.getNode().getTreePrev().getPsi() instanceof GrArgumentList)) {
        parentNode.removeChild(closure.getNode().getTreePrev());
      }
      parentNode.removeChild(closure.getNode());
      getArgumentList().replaceWithArgumentList(newArgList);
      GrExpression[] arguments = getArgumentList().getExpressionArguments();
      assert arguments.length == refIndex + 1;
      return arguments[refIndex];
    } else {
      return closure.replaceWithExpression(newExpr, true);
    }
  }

  public PsiMethod resolveMethod() {
    final GrExpression methodExpr = getInvokedExpression();
    if (methodExpr instanceof GrReferenceExpression) {
      final PsiElement resolved = ((GrReferenceExpression) methodExpr).resolve();
      return resolved instanceof PsiMethod ? (PsiMethod) resolved : null;
    }

    return null;
  }

  @NotNull
  public GroovyResolveResult[] getMethodVariants() {
    final GrExpression invoked = getInvokedExpression();
    if (!(invoked instanceof GrReferenceExpression)) return GroovyResolveResult.EMPTY_ARRAY;

    return ((GrReferenceExpression) invoked).getSameNameVariants();
  }

}