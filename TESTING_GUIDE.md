# Database Testing Guide

This guide will help you test the complete functionality of syncing dummy.json API data to your Supabase database.

## 🚀 Quick Start Testing

### Step 1: Set up Supabase Database
1. Go to your Supabase dashboard
2. Navigate to SQL Editor
3. Copy and paste the contents of `supabase_products_table.sql`
4. Run the script
5. Verify the `products` table was created in Table Editor

### Step 2: Test the App
1. Build and run your Android app
2. You'll see two new buttons in the main screen:
   - **"Test DB"** - Opens the test console
   - **"Sync DB"** - Opens the sync manager

## 🧪 Testing Scenarios

### Scenario 1: Test Dummy API Connection
**Goal**: Verify the app can fetch data from dummy.json API

**Steps**:
1. Click "Test DB" button
2. Click "Test Dummy API Connection"
3. **Expected Result**: 
   - ✅ Success message
   - 📊 Shows number of products fetched (should be 100)
   - 📦 Shows sample product details
   - 💰 Shows price, category, thumbnail

**If Failed**:
- Check internet connection
- Verify `https://dummyjson.com/products` is accessible
- Check Logcat for detailed error messages

### Scenario 2: Test Supabase Connection
**Goal**: Verify the app can connect to your Supabase database

**Steps**:
1. Click "Test DB" button
2. Click "Test Supabase Connection"
3. **Expected Result**:
   - ✅ Success message
   - 📊 Shows number of products in database (0 if empty)
   - 📭 "Database is empty - ready for sync" if no data

**If Failed**:
- Verify you ran the SQL script in Supabase dashboard
- Check your Supabase URL and anon key in the code
- Verify RLS policies are set correctly
- Check Logcat for detailed error messages

### Scenario 3: Test Full Sync Operation
**Goal**: Test the complete sync from dummy API to Supabase

**Steps**:
1. Click "Test DB" button
2. Click "Test Full Sync Operation"
3. **Expected Result**:
   - ✅ Sync operation successful
   - 📝 Shows "Successfully stored X products in database"
   - 🔄 Post-sync fetch successful
   - 📊 Database now contains X products

**If Failed**:
- Check Supabase table permissions
- Verify RLS policies allow insert operations
- Check Logcat for detailed error messages

### Scenario 4: Test Repository Integration
**Goal**: Test the repository pattern with Supabase

**Steps**:
1. Click "Sync DB" button
2. Click "Sync Products from Dummy API to Supabase"
3. Wait for sync to complete
4. Click "Toggle Data Source" to switch to Supabase
5. Click "Fetch Products"
6. **Expected Result**:
   - Shows products fetched from Supabase
   - Same data as dummy API but from your database

## 🔍 Detailed Testing

### Test 1: API Response Validation
```java
// Expected dummy.json response structure
{
  "products": [
    {
      "id": 1,
      "title": "iPhone 9",
      "description": "An apple mobile which is nothing like apple",
      "price": 549,
      "discountPercentage": 12.96,
      "rating": 4.69,
      "stock": 94,
      "brand": "Apple",
      "category": "smartphones",
      "thumbnail": "https://i.dummyjson.com/data/products/1/thumbnail.jpg",
      "images": ["https://i.dummyjson.com/data/products/1/1.jpg", ...]
    }
  ],
  "total": 100,
  "skip": 0,
  "limit": 30
}
```

### Test 2: Database Schema Validation
Verify your Supabase table has these columns:
- `id` (INTEGER, PRIMARY KEY)
- `title` (TEXT)
- `description` (TEXT)
- `price` (DECIMAL)
- `discount_percentage` (DECIMAL)
- `rating` (DECIMAL)
- `stock` (INTEGER)
- `brand` (TEXT)
- `category` (TEXT)
- `thumbnail` (TEXT)
- `images` (JSONB)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Test 3: Data Integrity
After sync, verify:
1. All 100 products from dummy API are in your database
2. Product IDs match (1-100)
3. All fields are properly populated
4. Images array contains valid URLs
5. Prices and ratings are numeric values

## 🐛 Troubleshooting

### Common Issues

#### Issue 1: "Products table not found"
**Solution**: Run the SQL script in your Supabase dashboard

#### Issue 2: "Failed to store products: 403"
**Solution**: Check RLS policies - ensure authenticated users can insert

#### Issue 3: "Network error"
**Solution**: Check internet connection and API availability

#### Issue 4: "JSON parsing error"
**Solution**: Check if dummy API response format changed

### Debug Information

#### Logcat Tags to Monitor:
- `ProductDatabaseManager` - Database operations
- `SupabaseManager` - Authentication operations
- `DatabaseTest` - Test activity logs

#### Key Log Messages:
```
✅ Dummy API test successful!
📊 Fetched 100 products
✅ Supabase connection successful!
📊 Found X products in database
✅ Sync operation successful!
📝 Successfully stored 100 products in database
```

## 📊 Performance Testing

### Test Sync Performance
1. Time the sync operation
2. Expected: < 30 seconds for 100 products
3. Monitor network usage
4. Check memory usage during sync

### Test Query Performance
1. Test search operations
2. Test category filtering
3. Test pagination (if implemented)
4. Monitor response times

## 🔒 Security Testing

### Test RLS Policies
1. Try accessing products without authentication
2. Try inserting products without authentication
3. Verify only authenticated users can modify data
4. Verify public read access works

### Test Data Validation
1. Verify no SQL injection possible
2. Check input sanitization
3. Verify JSON parsing is safe

## 📱 UI Testing

### Test Activity Navigation
1. Verify "Test DB" button opens DatabaseTestActivity
2. Verify "Sync DB" button opens DatabaseSyncActivity
3. Test back navigation
4. Test activity lifecycle

### Test UI Responsiveness
1. Test on different screen sizes
2. Test in different orientations
3. Test with slow network
4. Test with no network

## 🎯 Success Criteria

### ✅ All Tests Pass When:
1. **Dummy API Test**: Fetches 100 products successfully
2. **Supabase Test**: Connects and can read/write data
3. **Sync Test**: Successfully syncs all products
4. **Repository Test**: Can switch between data sources
5. **UI Test**: All buttons work and show proper feedback
6. **Performance Test**: Sync completes in reasonable time
7. **Security Test**: RLS policies work correctly

### 📋 Test Checklist
- [ ] Dummy API accessible and returns data
- [ ] Supabase table created with correct schema
- [ ] RLS policies configured correctly
- [ ] Sync operation works end-to-end
- [ ] Repository can switch data sources
- [ ] UI shows proper feedback
- [ ] Error handling works correctly
- [ ] Logging provides useful debug info

## 🚀 Next Steps After Testing

Once all tests pass:
1. **Production Ready**: The system is ready for production use
2. **Customization**: Modify the Product model as needed
3. **Scaling**: Consider pagination for large datasets
4. **Monitoring**: Add analytics and monitoring
5. **Backup**: Set up regular database backups

## 📞 Support

If you encounter issues:
1. Check Logcat for detailed error messages
2. Verify Supabase dashboard configuration
3. Test network connectivity
4. Review the troubleshooting section above
5. Check the comprehensive setup guide in `SUPABASE_DATABASE_SETUP.md` 