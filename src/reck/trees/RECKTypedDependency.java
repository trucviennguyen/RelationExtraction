package reck.trees;

import java.io.Serializable;

/**
 * A class extending the <code>TypedDependency</code> class, that is a 
 * relation between two words in a <code>GrammaticalStructure</code>.
 * Each <code>TypedDependency</code>
 * consists of a governor word, a dependent word, and a relation, which is
 * normally an instance of {@link GrammaticalRelation
 * <code>GrammaticalRelation</code>}.
 *
 * @author Bill MacCartney
 *
 * modified on 2009-04-28 by Truc-Vien T. Nguyen
 */
public class RECKTypedDependency implements Comparable<RECKTypedDependency>, Cloneable, Serializable {

  private String reln;
  private Integer gov;
  private Integer dep;

  public RECKTypedDependency(String reln, Integer gov, Integer dep) {
    this.reln = reln;
    this.gov = gov;
    this.dep = dep;
  }

  public RECKTypedDependency(Object reln, int gov, int dep) {
    this.reln = (String) reln;
    this.gov = new Integer(gov);
    this.dep = new Integer(dep);
  }

  public String reln() {
    return reln;
  }

  public Integer gov() {
    return gov;
  }

  public Integer dep() {
    return dep;
  }

  public void setReln(String reln) {
    this.reln = reln;
  }

  public void setGov(Integer gov) {
    this.gov = gov;
  }

  public void setDep(Integer dep) {
    this.dep = dep;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RECKTypedDependency)) {
      return false;
    }
    final RECKTypedDependency typedDep = (RECKTypedDependency) o;

    if (reln != null ? !reln.equals(typedDep.reln) : typedDep.reln != null) {
      return false;
    }
    if (gov.intValue() != -1 ? (gov.intValue() != typedDep.gov().intValue()) : typedDep.gov().intValue() != -1) {
      return false;
    }
    if (dep.intValue() != -1 ? (dep.intValue() != typedDep.dep().intValue()) : typedDep.dep().intValue() != -1) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = (reln != null ? reln.hashCode() : 17);
    result = 29 * result + (gov.intValue() != -1 ? gov.intValue() : 0);
    result = 29 * result + (dep.intValue() != -1 ? dep.intValue() : 0);
    return result;
  }

  public String toString() {
    return reln + "(" + gov.intValue() + ", " + dep.intValue() + ")";
  }


  public int compareTo(RECKTypedDependency tdArg) {
    int depArg = tdArg.dep().intValue();
    int depThis = this.dep().intValue();

    if (depThis > depArg) {
      return 1;
    } else if (depThis < depArg) {
      return -1;
    } else {
      return 0;
    }
  }

}
