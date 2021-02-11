/**
 * Value based entity support for native database.
 *
 * <p>Value based entities can use an implementation of {@link
 * com.akkaserverless.proxy.valueentity.store.Repository Repository} to properly persist the entity. The {@link
 * com.akkaserverless.proxy.valueentity.store.Repository Repository} should be provide with an implementation
 * of {@link com.akkaserverless.proxy.valueentity.store.Store Store} to properly access the native the database or
 * persistence.
 *
 * <p>Most part of the code in this package has been copied/adapted from https://github.com/akka/akka-persistence-jdbc.
 */
package com.akkaserverless.proxy.valueentity.store;
