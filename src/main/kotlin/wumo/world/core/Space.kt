package wumo.world.core

interface Space<E> {
  fun sample(): E
  fun contains(x: E): Boolean
}