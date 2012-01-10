/* Copyright (c) 2012, University of Edinburgh.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * This software is derived from (and contains code from) QTItools and MathAssessEngine.
 * QTItools is (c) 2008, University of Southampton.
 * MathAssessEngine is (c) 2010, University of Edinburgh.
 */
package org.qtitools.mathassess;

import static org.qtitools.mathassess.MathAssessConstants.ATTR_CODE_NAME;
import static org.qtitools.mathassess.MathAssessConstants.ATTR_SIMPLIFY_NAME;

import uk.ac.ed.ph.jqtiplus.attribute.value.BooleanAttribute;
import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.control.ItemProcessingContext;
import uk.ac.ed.ph.jqtiplus.control.JQTIExtensionPackage;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.validation.ValidationContext;
import uk.ac.ed.ph.jqtiplus.validation.AbstractValidationResult;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.RecordValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

import org.qtitools.mathassess.tools.qticasbridge.maxima.QTIMaximaSession;

import uk.ac.ed.ph.jacomax.MaximaTimeoutException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the <tt>org.qtitools.mathassess.CasCondition</tt> customOperator
 * 
 * @author Jonathon Hare
 */
public class CasCondition extends MathAssessOperator {

    private static final long serialVersionUID = -7534979343475172387L;

    private static final Logger logger = LoggerFactory.getLogger(CasCondition.class);

    public CasCondition(JQTIExtensionPackage jqtiExtensionPackage, ExpressionParent parent) {
        super(jqtiExtensionPackage, parent);

        getAttributes().add(new StringAttribute(this, getNamespacePrefix() + ATTR_CODE_NAME, null, null, true));
        getAttributes().add(new BooleanAttribute(this, getNamespacePrefix() + ATTR_SIMPLIFY_NAME, Boolean.FALSE, Boolean.FALSE, false));
    }

    /**
     * Gets value of code attribute.
     * 
     * @return value of code attribute
     * @see #setCode
     */
    public String getCode() {
        return getAttributes().getStringAttribute(getNamespacePrefix() + ATTR_CODE_NAME).getValue();
    }

    /**
     * Sets new value of code attribute.
     * 
     * @param code new value of code attribute
     * @see #getCode
     */
    public void setCode(String code) {
        getAttributes().getStringAttribute(getNamespacePrefix() + ATTR_CODE_NAME).setValue(code);
    }

    /**
     * Gets value of simplify attribute.
     * 
     * @return value of simplify attribute
     * @see #setSimplify
     */
    public Boolean getSimplify() {
        return getAttributes().getBooleanAttribute(getNamespacePrefix() + ATTR_SIMPLIFY_NAME)
                .getValue();
    }

    /**
     * Sets new value of simplify attribute.
     * 
     * @param simplify new value of simplify attribute
     * @see #getSimplify
     */
    public void setSimplify(Boolean simplify) {
        getAttributes().getBooleanAttribute(getNamespacePrefix() + ATTR_SIMPLIFY_NAME).setValue(
                simplify);
    }

    @Override
    protected void doAdditionalValidation(ValidationContext context, AbstractValidationResult result) {
        /* Nothing to do here */
    }

    @Override
    protected Value maximaEvaluate(ItemProcessingContext context) throws MaximaTimeoutException {
        final List<Value> childValues = getChildValues(context);
        final boolean simplify = getSimplify().booleanValue();
        final String code = getCode().trim();

        if (logger.isInfoEnabled()) {
            logger.info("Performing casCondition: code={}, simplify={}, values={}",
                    new Object[] { code, simplify, childValues });
        }

        final List<Value> values = new ArrayList<Value>();
        for (final Value v : childValues) {
            if (CasTypeGlue.isMathsContentRecord(v) && ((RecordValue) v).get(MathAssessConstants.FIELD_MAXIMA_IDENTIFIER) == null) {
                return NullValue.INSTANCE;
            }
            values.add(v);
        }

        final MathAssessExtensionPackage mathAssessExtensionPackage = (MathAssessExtensionPackage) getJQTIExtensionPackage();
        final QTIMaximaSession qtiMaximaSession = mathAssessExtensionPackage.obtainMaximaSessionForThread();
        return BooleanValue.valueOf(qtiMaximaSession.executeCasCondition(code, simplify, CasTypeGlue.convertFromJQTI(values)));
    }

    @Override
    public BaseType[] getProducedBaseTypes(ValidationContext context) {
        return new BaseType[] { BaseType.BOOLEAN };
    }

    @Override
    public Cardinality[] getProducedCardinalities(ValidationContext context) {
        return new Cardinality[] { Cardinality.SINGLE };
    }
}
