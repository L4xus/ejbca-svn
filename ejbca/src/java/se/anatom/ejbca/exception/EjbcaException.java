package se.anatom.ejbca.exception;

/**
 * Base for all specific application exceptions thrown by EJBCA.
 * Can be used to catch any non-crititcal application exceptions thay may be possible to handle:
 * <code>
 * try {
 *    .
 *    .
 *    .
 * } catch (EjbcaException e) {
 *     error(e.getMessage());
 *     ...
 * }
 * </code>
 *
 * @version $Id: EjbcaException.java,v 1.2 2002-04-07 09:55:29 anatom Exp $
 */
public class EjbcaException extends Exception {

   /**
    * Constructor used to create exception with an errormessage.
    * Calls the same constructor in baseclass <code>Exception</code>.
    *
    * @param message Human redable error message, can not be NULL.
    */
   public EjbcaException(String message) {
       super(message);
   }
   /**
    * Constructor used to create exception with an embedded exception.
    * Calls the same constructor in baseclass <code>Exception</code>.
    *
    * @param exception exception to be embedded.
    */
   public EjbcaException(Exception exception) {
       super(exception);
   }
}
