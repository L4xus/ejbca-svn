/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ui.cli.infrastructure.parameter;

import org.ejbca.ui.cli.infrastructure.parameter.enums.MandatoryMode;
import org.ejbca.ui.cli.infrastructure.parameter.enums.ParameterMode;


/**
 * Wrapper class for a command parameter
 * 
 * @version $Id: Parameter.java 17528 2013-08-27 13:31:03Z mikekushner $
 *
 */
public class Parameter {
    
    private final String keyWord;
    private final boolean allowStandAlone;
    private final ParameterMode parameterMode;
    private final MandatoryMode mandatoryMode;
    private final String instruction;
    private final String name;
    
    /**
     * Constructor for defining a parameter
     * 
     * @param keyWord The keyword used to identify this parameter. Commonly prefixed with a dash ('-')
     * @param name What this parameter denotes. Used for documentation purposes.
     * @param mandatoryMode Defines whether this parameter is mandatory or not. 
     * @param allowStandAlone true if this parameter can be inputed without its keyword. 
     * @param parameterMode 
     */
    public Parameter(String keyWord, String name, MandatoryMode mandatoryMode, boolean allowStandAlone, ParameterMode parameterMode, String instruction) {
        this.keyWord = keyWord;
        this.allowStandAlone = allowStandAlone;
        this.parameterMode = parameterMode;
        this.mandatoryMode = mandatoryMode;
        this.instruction = instruction;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public boolean isMandatory() {
        return mandatoryMode.isMandatory();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyWord == null) ? 0 : keyWord.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Parameter other = (Parameter) obj;
        if (keyWord == null) {
            if (other.keyWord != null)
                return false;
        } else if (!keyWord.equals(other.keyWord))
            return false;
        return true;
    }

    public ParameterMode getParameterMode() {
        return parameterMode;
    }

    public boolean allowStandAlone() {
        return allowStandAlone;
    }

    /**
     * @return the keyWord
     */
    public String getKeyWord() {
        return keyWord;
    }

    public String getInstruction() {
        return instruction;
    }
    
    
}
