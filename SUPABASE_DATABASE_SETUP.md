# Supabase Database Setup with Dummy.json API

This guide will help you set up a Supabase database and sync data from the dummy.json API to your own database.

## Prerequisites

1. A Supabase account and project
2. Your Supabase URL and anon key (already configured in the code)
3. Android Studio with your project open

## Step 1: Create the Products Table in Supabase

1. Go to your Supabase dashboard
2. Navigate to the SQL Editor
3. Copy and paste the contents of `supabase_products_table.sql`
4. Run the SQL script

This will create:
- A `products` table with all necessary columns
- Indexes for better performance
- Row Level Security (RLS) policies
- Automatic timestamp updates

## Step 2: Database Schema

The products table includes the following columns:

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, product ID |
| title | TEXT | Product title |
| description | TEXT | Product description |
| price | DECIMAL(10,2) | Product price |
| discount_percentage | DECIMAL(5,2) | Discount percentage |
| rating | DECIMAL(3,2) | Product rating |
| stock | INTEGER | Available stock |
| brand | TEXT | Product brand |
| category | TEXT | Product category |
| thumbnail | TEXT | Thumbnail image URL |
| images | JSONB | Array of product images |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

## Step 3: Using the Database Sync Activity

### Launch the Activity

Add this to your MainActivity or create a button to launch the DatabaseSyncActivity:

```java
Intent intent = new Intent(this, DatabaseSyncActivity.class);
startActivity(intent);
```

### Features Available

1. **Sync Products**: Fetches all products from dummy.json API and stores them in your Supabase database
2. **Toggle Data Source**: Switch between using Supabase database and the original dummy API
3. **Fetch Products**: Test fetching products from the current data source
4. **Search Products**: Test searching products (currently searches for "phone")

## Step 4: Integration with Your Existing Code

### Using ProductDatabaseManager Directly

```java
// Initialize the manager
ProductDatabaseManager.initialize(context);

// Sync products from dummy API to Supabase
ProductDatabaseManager.syncProductsFromDummyApi(new ProductDatabaseManager.DatabaseCallback() {
    @Override
    public void onSuccess(String message) {
        // Products synced successfully
        Log.d("Database", message);
    }
    
    @Override
    public void onError(String error) {
        // Handle error
        Log.e("Database", error);
    }
});

// Fetch products from Supabase
ProductDatabaseManager.fetchProductsFromSupabase(new ProductDatabaseManager.ProductCallback() {
    @Override
    public void onSuccess(List<Product> products) {
        // Use the products
        for (Product product : products) {
            Log.d("Product", product.getTitle());
        }
    }
    
    @Override
    public void onError(String error) {
        // Handle error
        Log.e("Database", error);
    }
});
```

### Using the Repository Pattern

```java
// Initialize repository
ProductApiService apiService = NetworkManager.getRetrofitInstance().create(ProductApiService.class);
ProductRepository repository = new ProductRepositoryImpl(apiService);

// Sync products
repository.syncProductsFromDummyApi();

// Toggle between Supabase and API
repository.setUseSupabase(true); // Use Supabase
repository.setUseSupabase(false); // Use dummy API

// Fetch products (uses current data source)
List<Product> products = repository.getProducts();

// Search products
List<Product> searchResults = repository.searchProducts("phone");

// Get specific product
Product product = repository.getProductById(1);

// Update product
product.setPrice(99.99);
repository.updateProduct(product);

// Delete product
repository.deleteProduct(1);
```

## Step 5: Advanced Features

### Custom Search Queries

```java
// Search by category
ProductDatabaseManager.searchProductsInSupabase("smartphones", callback);

// Search by brand
ProductDatabaseManager.searchProductsInSupabase("Apple", callback);

// Search by description
ProductDatabaseManager.searchProductsInSupabase("wireless", callback);
```

### Batch Operations

```java
// Fetch products from dummy API
ProductDatabaseManager.fetchProductsFromDummyApi(new ProductDatabaseManager.ProductCallback() {
    @Override
    public void onSuccess(List<Product> products) {
        // Process products before storing
        for (Product product : products) {
            // Modify products if needed
            product.setPrice(product.getPrice() * 1.1); // Add 10% markup
        }
        
        // Store modified products
        ProductDatabaseManager.storeProductsInSupabase(products, callback);
    }
    
    @Override
    public void onError(String error) {
        // Handle error
    }
});
```

## Step 6: Security and Permissions

The SQL script sets up Row Level Security (RLS) with the following policies:

- **Public Read Access**: Anyone can read products
- **Authenticated Write Access**: Only authenticated users can create, update, or delete products

### Customizing Permissions

If you want to restrict read access to authenticated users only, modify the SQL:

```sql
-- Remove public read access
DROP POLICY "Allow public read access to products" ON products;

-- Add authenticated read access
CREATE POLICY "Allow authenticated read access to products" ON products
    FOR SELECT USING (auth.role() = 'authenticated');
```

## Step 7: Monitoring and Debugging

### Check Database Status

1. Go to your Supabase dashboard
2. Navigate to Table Editor
3. Select the `products` table
4. View the data and monitor changes

### Logs and Debugging

The code includes comprehensive logging. Check Logcat for:
- `ProductDatabaseManager` tags for database operations
- `SupabaseManager` tags for authentication operations

### Common Issues

1. **Network Errors**: Check internet connection and API availability
2. **Authentication Errors**: Verify Supabase credentials
3. **Permission Errors**: Check RLS policies in Supabase dashboard
4. **Data Type Errors**: Ensure JSON structure matches the Product model

## Step 8: Performance Optimization

### Indexes

The SQL script creates indexes on:
- `category` - for category-based queries
- `brand` - for brand-based queries  
- `price` - for price-based sorting
- `rating` - for rating-based sorting

### Pagination

For large datasets, consider implementing pagination:

```java
// Example pagination query
String url = SUPABASE_URL + "/rest/v1/products?select=*&limit=20&offset=40";
```

## Step 9: Data Synchronization

### Regular Sync

Consider implementing a background service for regular synchronization:

```java
// Example: Sync every 6 hours
Handler handler = new Handler();
Runnable syncRunnable = new Runnable() {
    @Override
    public void run() {
        ProductDatabaseManager.syncProductsFromDummyApi(callback);
        handler.postDelayed(this, 6 * 60 * 60 * 1000); // 6 hours
    }
};
handler.post(syncRunnable);
```

### Incremental Updates

For production use, consider implementing incremental updates instead of full syncs to reduce data transfer and processing time.

## Support

If you encounter any issues:

1. Check the Supabase dashboard for table structure and data
2. Verify network connectivity and API availability
3. Review Logcat for detailed error messages
4. Ensure all dependencies are properly included in your build.gradle

The implementation provides a complete solution for syncing dummy.json API data to your own Supabase database with full CRUD operations, search functionality, and flexible data source switching. 