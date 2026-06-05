package com.acme.modres.db;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModResortsCustomerInformationTest {

    @InjectMocks
    private ModResortsCustomerInformation customerInformation;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void testGetCustomerInformation_withValidData_shouldReturnCustomerList() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("INFO")).thenReturn("Customer1", "Customer2");

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Customer1", result.get(0));
        assertEquals("Customer2", result.get(1));
    }

    @Test
    void testGetCustomerInformation_withNoData_shouldReturnEmptyList() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomerInformation_withSingleRecord_shouldReturnSingleItem() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("INFO")).thenReturn("SingleCustomer");

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SingleCustomer", result.get(0));
    }

    @Test
    void testGetCustomerInformation_withSQLException_shouldReturnEmptyList() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomerInformation_withNullDataSource_shouldReturnEmptyList() {
        // Arrange
        ModResortsCustomerInformation customerInfoWithNullDS = new ModResortsCustomerInformation();

        // Act
        ArrayList<String> result = customerInfoWithNullDS.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomerInformation_shouldCloseResources() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("INFO")).thenReturn("Customer1");

        // Act
        customerInformation.getCustomerInformation();

        // Assert
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void testGetCustomerInformation_withResultSetException_shouldCloseResources() throws SQLException {
        // Arrange
        when(resultSet.next()).thenThrow(new SQLException("ResultSet error"));

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void testGetCustomerInformation_withMultipleRecords_shouldReturnAllRecords() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, true, true, false);
        when(resultSet.getString("INFO"))
            .thenReturn("Customer1", "Customer2", "Customer3", "Customer4");

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void testGetCustomerInformation_shouldExecuteCorrectQuery() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        customerInformation.getCustomerInformation();

        // Assert
        verify(connection).prepareStatement("SELECT INFO FROM CUSTOMER");
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetCustomerInformation_withNullInfo_shouldHandleGracefully() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("INFO")).thenReturn(null);

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    @Test
    void testGetCustomerInformation_withEmptyString_shouldReturnEmptyString() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("INFO")).thenReturn("");

        // Act
        ArrayList<String> result = customerInformation.getCustomerInformation();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test
    void testGetCustomerInformation_withCloseException_shouldNotThrowException() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("INFO")).thenReturn("Customer1");
        doThrow(new SQLException("Close failed")).when(resultSet).close();

        // Act & Assert
        assertDoesNotThrow(() -> customerInformation.getCustomerInformation());
    }
}
