package com.example.mobilegpt.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobilegpt.R
import com.example.mobilegpt.socket.ClientSocket
import com.example.mobilegpt.socket.ClientSocketUser
import com.example.mobilegpt.ui.theme.MobileGPTTheme
import com.example.mobilegpt.viewmodel.ChatViewModel
import com.example.mobilegpt.viewmodel.global
import kotlinx.coroutines.runBlocking


class ChatActivity : ComponentActivity(), ClientSocketUser {
    private val chatViewModel: ChatViewModel by viewModels()
    private var conversationList: SnapshotStateList<Message> = mutableStateListOf()

    private var clientSocket : ClientSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        clientSocket = global.clientSocket
        clientSocket!!.addObserver(this)

        setContent {
            MobileGPTTheme {
                BuildScreen()
            }
        }
    }

    fun addSocket(clientSocket : ClientSocket){
        this.clientSocket = clientSocket
    }

    @Composable
    fun BuildScreen(){
        Column(){
            Surface(modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)){
                Conversation()
            }

            Surface(modifier = Modifier
                .fillMaxWidth()){
                TextSend()
            }
        }
    }

    @Composable
    fun Conversation() {
        val lst: List<Message> = this.conversationList
        LazyColumn (){
            items(lst) {
                    message -> MessageCard(message)
            }
        }
    }

    @Preview
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TextSend() {
        var msg by remember {
            mutableStateOf(TextFieldValue(""))
        }

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(all = 8.dp)
        ){
            OutlinedTextField(
                value = msg,
                onValueChange = { newValue -> msg = newValue},
                modifier = Modifier
                    .weight(1f)
                    .width(200.dp)
                    .height(56.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    conversationList.add(Message("user", msg.text))
                    runBlocking {
                        clientSocket?.send(global.CHAT, msg.text)
                    }
                    msg = TextFieldValue("")
                },
                modifier = Modifier
                    .width(70.dp)
                    .height(40.dp),
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                    contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "Contact profile picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .requiredSize(30.dp)
                    )
                }
            }
        }
    }
    
    data class Message(val author: String, val body: String)

    @Composable
    fun MessageCard(msg: Message) {
        if (msg.author == "MobileGPT"){
            Row(modifier = Modifier.padding(all = 8.dp)) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "profile picture",
                    modifier = Modifier
                        .requiredSize(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.Start) {
                    Text(text = msg.author,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall)

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shadowElevation = 10.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(text = msg.body,
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .widthIn(10.dp, 280.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        else{
            Row(
                modifier = Modifier.padding(all = 8.dp)) {
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.End) {
                    Text(text = msg.author,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall)

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shadowElevation = 10.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(text = msg.body,
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .widthIn(10.dp, 280.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun PreviewMessageCard() {
        MobileGPTTheme {
            Surface {
                MessageCard(
                    msg = Message("Lexi", "Take a look at Jetpack Compose, it's great!")
                )
            }
        }
    }
    
    var test =  listOf(
                Message("MobileGPT", "hellohellohellohellohellohellohellohellohellohellohellohello"),
                Message("User", "whatupwhatupwhatupwhatupwhatupwhatupwhatupup"),
                Message("MobileGPT", "hello"),
                Message("User", "what"),
                Message("MobileGPT", "hellohellohellohellohellohellohellohellohellohellohellohellohellohellohello"),
                Message("User", "whatwhatwhatwhatwhatwhatwhatwhatwhatwhatwhatwhatwhat"),
                Message("MobileGPT", "hello"),
                Message("MobileGPT", "hellohellohellohellohellohellohellohellohellohellohellohello"),
                Message("User", "whatupwhatupwhatupwhatupwhatupwhatupwhatupup"),
                Message("MobileGPT", "hello"),
                Message("User", "what"),

                    )
    @Preview
    @Composable
    fun PreviewScreen() {
        MobileGPTTheme {
            BuildScreen()
        }
    }

    override fun onMsg(head: String, body: String) {
        if (head == global.CHAT)
        {
            conversationList.add(Message("MobileGPT", body))
        }
    }
}



