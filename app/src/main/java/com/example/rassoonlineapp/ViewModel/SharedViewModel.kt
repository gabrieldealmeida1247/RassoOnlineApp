package com.example.rassoonlineapp.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rassoonlineapp.Model.Proposals

class SharedViewModel : ViewModel() {
    val acceptedProposal = MutableLiveData<Proposals>()
}