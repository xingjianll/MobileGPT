package com.example.mobilegpt.activity

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.mobilegpt.R
import com.example.mobilegpt.socket.ClientSocket
import com.example.mobilegpt.ui.theme.MobileGPTTheme
import com.example.mobilegpt.viewmodel.LoginViewModel
import com.example.mobilegpt.viewmodel.global
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.InternalAPI
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {

    private val viewModel : LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        subscribeToEvents()

        setContent {
            MobileGPTTheme {
                LoginScreen()
            }
        }

        var clientSocket = ClientSocket(mutableListOf(viewModel))
        runBlocking {
            try {clientSocket.setUp()} catch (e: java.net.ConnectException) {showToast("No connection to the server.")}
        }
        GlobalScope.launch (Dispatchers.IO){

            try {clientSocket.listen()} catch (_: UninitializedPropertyAccessException) {}
        }
        viewModel.addSocket(clientSocket)
        global.clientSocket = clientSocket
        Log.d("MyApp", "backstage online")
    }

    @Preview(showBackground = true)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {
        val pad = 36.dp

        var username by remember {
            mutableStateOf(TextFieldValue(""))
        }

        var password by remember {
            mutableStateOf(TextFieldValue(""))
        }

        var showProgress: Boolean by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pad, bottom = pad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
                    .clip(CircleShape)
            )

            Spacer(
                modifier = Modifier
                    .size(pad)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { newValue -> username = newValue },
                label = { Text(text = "Enter username") },
                singleLine = true,
                modifier = Modifier
                    .width(200.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { newValue -> password = newValue },
                label = { Text(text = "Enter password") },
                singleLine = true,
                modifier = Modifier
                    .width(200.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(
                modifier = Modifier
                    .size(pad)
            )

            Button(
                onClick = {viewModel.loginUser(username.text, password.text)},
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Login")
            }

            if (showProgress) {
                CircularProgressIndicator()
            }
        }
    }
    
    private fun subscribeToEvents(){
        lifecycleScope.launchWhenStarted{
            viewModel.loginEvent.collect{event ->
                when (event){
                    is LoginViewModel.LogInEvent.ErrorInputTooShort -> {
                        showToast("Too short password or username")
                    }
                    is LoginViewModel.LogInEvent.ErrorLogIn -> {
                        showToast("Error")
                    }
                    is LoginViewModel.LogInEvent.Success -> {
                        Log.d("MyApp", "switch screen")
                        val switchActivityIntent = Intent(this@MainActivity, ChatActivity::class.java)
                        startActivity(switchActivityIntent)
                    }
                    is LoginViewModel.LogInEvent.Failure -> {
                        showToast("Wrong username or password")
                    }
                }
            }
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        runBlocking{
//            global.clientSocket.send("02", "")
        }
        global.clientSocket.close()
        super.onDestroy()
    }
}


