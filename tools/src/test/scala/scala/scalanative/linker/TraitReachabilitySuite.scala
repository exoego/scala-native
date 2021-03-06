package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class TraitReachabilitySuite extends ReachabilitySuite {
  val Test            = g("Test$")
  val TestInit        = g("Test$", "init")
  val TestMain        = g("Test$", "main_unit")
  val TestCallFoo     = g("Test$", "callFoo_Parent_unit")
  val Parent          = g("Parent")
  val ParentClass     = g("Parent$class")
  val ParentClassInit = g("Parent$class", "$init$_Parent_unit")
  val ParentClassFoo  = g("Parent$class", "foo_Parent_unit")
  val Child           = g("Child")
  val ChildInit       = g("Child", "init")
  val ChildFoo        = g("Child", "foo_unit")
  val GrandChild      = g("GrandChild")
  val GrandChildInit  = g("GrandChild", "init")
  val GrandChildFoo   = g("GrandChild", "foo_unit")
  val Object          = g("java.lang.Object")
  val ObjectInit      = g("java.lang.Object", "init")

  testReachable("unused traits are discarded") {
    val source = """
      trait Parent
      class Child extends Parent

      object Test {
        def main: Unit = ()
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("inherited trait is included") {
    val source = """
      trait Parent
      class Child extends Parent

      object Test {
        def main: Unit = new Child
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Child,
      ChildInit,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on trait includes an impl of all implementors (1)") {
    val source = """
      trait Parent {
        def foo: Unit
      }
      class Child extends Parent {
        def foo: Unit = ()
      }

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
        def main: Unit =
          callFoo(new Child)
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on trait includes an impl of all implementors (2)") {
    val source = """
      trait Parent {
        def foo: Unit
      }
      class Child extends Parent {
        def foo: Unit = ()
      }
      class GrandChild extends Child {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
        def main: Unit = {
          callFoo(new Child)
          callFoo(new GrandChild)
        }
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      GrandChild,
      GrandChildInit,
      GrandChildFoo,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on a trait with default implemention includes impl class") {
    val source = """
      trait Parent {
        def foo: Unit = ()
      }
      class Child extends Parent

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
        def main: Unit =
          callFoo(new Child)
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Parent,
      ParentClass,
      ParentClassInit,
      ParentClassFoo,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on a trait with default implemention discards impl class") {
    val source = """
      trait Parent {
        def foo: Unit = ()
      }
      class Child extends Parent {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
        def main: Unit =
          callFoo(new Child)
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      ParentClass,
      ParentClassInit,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }
}
