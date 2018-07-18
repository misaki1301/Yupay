package com.shibuyaxpress.yupay.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.*
import com.shibuyaxpress.yupay.GlideApp
import com.shibuyaxpress.yupay.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class NewClothActivity : AppCompatActivity() {
    var imageBitmap:Bitmap? = null
    val EXTRA_REPLY:String="com.shibuyaxpress.yupay.REPLY"
    private var editClothName: EditText? = null
    private var editClothPrice: EditText? = null
    private var imageView: ImageView? = null
    private var addImage: ImageButton? = null
    private var saveButton: Button? = null
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_cloth)

        editClothName = findViewById(R.id.edit_name_cloth)
        editClothPrice = findViewById(R.id.edit_price_cloth)
        saveButton = findViewById(R.id.button_save)
        imageView = findViewById(R.id.imageCloth)
        addImage = findViewById(R.id.addImage)

        /*saveButton!!.setOnClickListener {
            val replyIntent = Intent()
            when {
                TextUtils.isEmpty(mEditClothName!!.text) -> setResult(Activity.RESULT_CANCELED,replyIntent)
                TextUtils.isEmpty(mEditClothPrice!!.text) -> setResult(Activity.RESULT_CANCELED,replyIntent)
                else -> {
                    val cloth:String=mEditClothName!!.text.toString()
                    Log.d("dato ropa",cloth)
                    replyIntent.putExtra(EXTRA_REPLY,cloth)
                    setResult(Activity.RESULT_OK,replyIntent)
                }
            }
            finish()
        }*/

        saveButton!!.setOnClickListener {
            when {
                TextUtils.isEmpty(editClothName!!.text) -> editClothName!!.error = "Ingrese el nombre de la ropa"
                TextUtils.isEmpty(editClothPrice!!.text) -> editClothPrice!!.error = "Ingrese un precio al producto"
                imageBitmap == null -> Toast.makeText(this@NewClothActivity,"Seleccione una imagen",
                        Toast.LENGTH_SHORT).show()
                else->{
                    saveDataForCloth()
                    finish()
                }
            }
        }
        addImage!!.setOnClickListener {
            showPictureDialog()
        }


    }
    private fun showPictureDialog(){
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Seleccione una acción")
        val pictureDialogItems = arrayOf("Seleccionar imagen de la galeria",
                "Tomar foto con la cámara")
        pictureDialog.setItems(pictureDialogItems){dialog, which ->
            when(which){
                0->choosePhotoFromGallery()
                1->takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY)
    }

    private fun takePhotoFromCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    private fun setValueForImageBitmap(bitmap:Bitmap){
        imageBitmap=bitmap
    }
    private fun saveDataForCloth(){
        val replyIntent = Intent()
        val name = editClothName!!.text.toString()
        val price = editClothPrice!!.text.toString().toDouble()
        val imagePath = saveImage(imageBitmap!!)
        replyIntent.putExtra("name",name)
        replyIntent.putExtra("price",price)
        replyIntent.putExtra("image",imagePath)
        Log.d("Bundle+IMAGE", replyIntent.extras.toString()+" ${imagePath}")
        setResult(Activity.RESULT_OK,replyIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GALLERY->{
                if(data != null){
                    val contentURI = data.data
                    try{
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        GlideApp.with(this)
                                .load(bitmap)
                                .placeholder(R.drawable.ic_image_black_24dp)
                                .fitCenter()
                                .into(imageView!!)
                        //val path = saveImage(bitmap)
                        //Log.d("TAG", path::class.simpleName)
                        //Toast.makeText(this@NewClothActivity,
                        //       "imagen guardada!-->route::${path}",Toast.LENGTH_SHORT).show()
                        //imageView!!.setImageBitmap(bitmap)
                        setValueForImageBitmap(bitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this@NewClothActivity,"Fallo!",Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
            CAMERA->{
                try{
                    val thumbnail = data!!.extras!!.get("data") as Bitmap
                    GlideApp.with(this)
                            .load(thumbnail)
                            .placeholder(R.drawable.ic_image_black_24dp)
                            .fitCenter()
                            .into(imageView!!)
                    //imageView!!.setImageBitmap(thumbnail)
                    //saveImage(thumbnail)
                    //Toast.makeText(this@NewClothActivity,"imagen guardada",
                    //       Toast.LENGTH_SHORT).show()
                    setValueForImageBitmap(thumbnail)
                }catch (e:NullPointerException){
                    e.printStackTrace()
                    Toast.makeText(this@NewClothActivity,"failure!",Toast.LENGTH_SHORT)
                            .show()
                }
            }
        }
    }

    private fun saveImage(myBitmap:Bitmap):String{
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
        val wallpaperDirectory = File(
                (Environment.getExternalStorageDirectory()).toString()+IMAGE_DIRECTORY)
        Log.d("fee",wallpaperDirectory.toString())
        if(!wallpaperDirectory.exists()){
            wallpaperDirectory.mkdirs()
        }
        try{
            Log.d("heel", wallpaperDirectory.toString())
            val file = File(wallpaperDirectory,((Calendar.getInstance().timeInMillis).toString()+".jpg"))
            file.createNewFile()
            val fo = FileOutputStream(file)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("image/jpg"),null)
            fo.close()
            Log.d("TAG", "File Saved::--->${file.absolutePath}")
            return file.absolutePath
        }catch (e1:IOException){
            e1.printStackTrace()
        }
        return ""
    }
    companion object {
        private const val IMAGE_DIRECTORY = "/yupay"
    }
}
