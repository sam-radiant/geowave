package mil.nga.giat.geowave.datastore.dynamodb.operations;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import mil.nga.giat.geowave.core.store.adapter.DataAdapter;
import mil.nga.giat.geowave.core.store.entities.GeoWaveRow;
import mil.nga.giat.geowave.core.store.operations.Deleter;
import mil.nga.giat.geowave.datastore.dynamodb.DynamoDBRow;

public class DynamoDBDeleter implements Deleter
{
	private static Logger LOGGER = LoggerFactory.getLogger(DynamoDBDeleter.class);

	private final AmazonDynamoDBAsyncClient client;
	private final String tableName;
	
	public DynamoDBDeleter(
			final AmazonDynamoDBAsyncClient client,
			final String qualifiedTableName) {
		this.client = client;
		this.tableName = qualifiedTableName;
	}

	@Override
	public void close()
			throws Exception {
	}

	@Override
	public void delete(
			GeoWaveRow row,
			DataAdapter<?> adapter ) {
		DynamoDBRow dynRow = (DynamoDBRow)row;
		
		DeleteItemResult result = client.deleteItem(
				tableName,
				Maps.filterEntries(
						dynRow.getAttributeMapping(),
						new Predicate<Entry<String, AttributeValue>>() {
							@Override
							public boolean apply(
									final Entry<String, AttributeValue> input ) {
								return DynamoDBRow.GW_PARTITION_ID_KEY.equals(
										input.getKey())
										|| DynamoDBRow.GW_RANGE_KEY.equals(
												input.getKey());
							}
						}));
		
		if (result == null || result.getAttributes() == null || result.getAttributes().isEmpty()) {
			LOGGER.error("Failed to delete row");
		}
	}

}
