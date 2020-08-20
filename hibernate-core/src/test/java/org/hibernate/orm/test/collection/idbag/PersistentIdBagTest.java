/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.collection.idbag;

import java.util.ArrayList;

import org.hibernate.collection.internal.PersistentIdentifierBag;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests related to operations on a PersistentIdentifierBag
 *
 * @author Steve Ebersole
 */
@DomainModel(
		xmlMappings = "org/hibernate/orm/test/collection/idbag/Mappings.hbm.xml"
)
@SessionFactory
public class PersistentIdBagTest {

	@Test
	public void testWriteMethodDirtying(SessionFactoryScope scope) {
		IdbagOwner parent = new IdbagOwner( "root" );
		IdbagOwner child = new IdbagOwner( "c1" );
		parent.getChildren().add( child );
		IdbagOwner otherChild = new IdbagOwner( "c2" );

		scope.inTransaction(
				session -> {
					session.save( parent );
					session.flush();
					// at this point, the list on parent has now been replaced with a PersistentBag...
					PersistentIdentifierBag children = (PersistentIdentifierBag) parent.getChildren();

					assertFalse( children.remove( otherChild ) );
					assertFalse( children.isDirty() );

					ArrayList otherCollection = new ArrayList();
					otherCollection.add( child );
					assertFalse( children.retainAll( otherCollection ) );
					assertFalse( children.isDirty() );

					otherCollection = new ArrayList();
					otherCollection.add( otherChild );
					assertFalse( children.removeAll( otherCollection ) );
					assertFalse( children.isDirty() );

					children.clear();
					session.delete( child );
					assertTrue( children.isDirty() );

					session.flush();

					children.clear();
					assertFalse( children.isDirty() );

					session.delete( parent );
				}
		);
	}
}
