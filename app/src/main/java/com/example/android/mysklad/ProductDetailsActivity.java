package com.example.android.mysklad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private DatabaseReference mydatabase;
    private FirebaseStorage storage;
    private StorageReference imageRef;
    private ImageView imageProduct;
    private EditText titleProduct, priceProduct;
    private Button saveBtn, deleteBtn;
    private ProgressBar progressBar;
    private final static int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    static final int GALLERY_REQUEST = 1;
    private String imageURL;
    private String productId;
    private boolean isImageUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        getSupportActionBar().setTitle(getString(R.string.redact_product));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mydatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        imageRef = storage.getReference();

        productId = Buffer.product.getId();

        imageProduct = (ImageView) findViewById(R.id.image);
        titleProduct = (EditText) findViewById(R.id.title_et);
        priceProduct = (EditText) findViewById(R.id.price_et);
        saveBtn = (Button) findViewById(R.id.save_btn);
        deleteBtn = (Button) findViewById(R.id.delete_btn);
        progressBar = (ProgressBar)findViewById(R.id.progress_circular);
        progressBar.setVisibility(View.VISIBLE);

        Picasso.get().
                load(Buffer.product.getPhotoUrl()).
                into(imageProduct, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        titleProduct.setText(Buffer.product.getTitle());
        priceProduct.setText(Buffer.product.getPrice()+"");

        imageProduct.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        findViewById(R.id.parent_layout).setOnClickListener(this);

        setNotClickableSaveBtn();

        titleProduct.addTextChangedListener(textWatcher);
        priceProduct.addTextChangedListener(textWatcher);

        titleProduct.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    hideKeyboard(titleProduct);
                }
                return true;
            }
        });

    }
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            checkFields();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            checkFields();
        }
    };
    private void setClickableSaveBtn(){
        saveBtn.getBackground().setAlpha(250);
        saveBtn.setEnabled(true);
        saveBtn.setClickable(true);
    }
    private void setNotClickableSaveBtn(){
        saveBtn.getBackground().setAlpha(150);
        saveBtn.setEnabled(false);
        saveBtn.setClickable(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_btn:
                if (isImageUpload){
                    progressBar.setVisibility(View.VISIBLE);
                    uploadpic(productId);
                }else {updateProduct();}
                break;
            case R.id.delete_btn:
                deleteProduct();
                break;
            case R.id.image:
                checkPermission();
                break;
            case R.id.parent_layout:
                hideKeyboard(titleProduct);
                break;
        }
    }

    private void deleteProduct() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        builder.setTitle("Удалить товар?")
                .setCancelable(false)
                .setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mydatabase.child("products").child(productId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProductDetailsActivity.this, "Товар удален",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }
    private void uploadpic(String key) {

        final StorageReference productImageRef= imageRef.child("productImage").child(key+".jpg");

        // Get the data from an ImageView as bytes
        imageProduct.setDrawingCacheEnabled(true);
        imageProduct.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageProduct.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = productImageRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();

                }
                // Continue with the task to get the download URL
                return productImageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageURL =task.getResult().toString();
                    updateProduct();


                } else {
                    Toast.makeText(ProductDetailsActivity.this, "ошибка! Изображение  не загружено!",
                            Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    private void updateProduct() {
        Buffer.product.setTitle(titleProduct.getText().toString());
        Buffer.product.setPrice(Float.parseFloat(priceProduct.getText().toString()));
        if (imageURL!=null) {
            Buffer.product.setPhotoUrl(imageURL);
        }
        Map<String, Object> update = Buffer.product.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/products/" + productId, update);


        mydatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProductDetailsActivity.this, "Изменения успешно сохранены", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailsActivity.this, "Ошибка! Изменения не сохранены!", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void checkPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(ProductDetailsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            showGallery();
        } else {
            ActivityCompat.requestPermissions(ProductDetailsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showGallery();
                }
                break;
        }
    }

    public void showGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    progressBar.setVisibility(View.VISIBLE);
                    Uri selectedImageUri = imageReturnedIntent.getData();

                    Picasso.get()
                            .load(selectedImageUri)
                            .into(imageProduct, new Callback() {
                                @Override
                                public void onSuccess() {
                                    isImageUpload=true;
                                    progressBar.setVisibility(View.GONE);
                                    checkFields();

                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(ProductDetailsActivity.this, "ошибка.изображение не загружено",
                                            Toast.LENGTH_SHORT).show();

                                }

                            });
                }
        }
    }

    private void checkFields(){
        if (!titleProduct.getText().toString().equals(Buffer.product.getTitle()) || !priceProduct.getText().toString().equals(Buffer.product.getPrice()+"") || isImageUpload) {
            if (!titleProduct.getText().toString().isEmpty() && !priceProduct.getText().toString().isEmpty()) {
                setClickableSaveBtn();
            }else {setNotClickableSaveBtn();}

        } else {setNotClickableSaveBtn();}
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
