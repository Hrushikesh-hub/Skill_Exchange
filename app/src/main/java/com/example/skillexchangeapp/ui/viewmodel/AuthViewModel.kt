package com.example.skillexchangeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillexchangeapp.data.firebase.FirebaseService
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.data.repository.SkillExchangeRepository
import com.example.skillexchangeapp.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: SkillExchangeRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Try Firebase Auth first
                FirebaseService.signIn(email.trim().lowercase(), password)
                // Fall through to get local user record
                val user = repository.getUserByEmail(email.trim().lowercase())
                if (user != null) {
                    _authState.value = AuthState.Success(user.id)
                } else {
                    // Firebase auth succeeded but no local record (edge case) — check hash
                    _authState.value = AuthState.Error("Account not found. Please register.")
                }
            } catch (_: Exception) {
                // Firebase failed — try local hash fallback (offline mode)
                val user = repository.getUserByEmail(email.trim().lowercase())
                if (user != null && user.passwordHash == SecurityUtils.hashPassword(password)) {
                    _authState.value = AuthState.Success(user.id)
                } else {
                    _authState.value = AuthState.Error("Invalid email or password")
                }
            }
        }
    }

    fun register(user: User, plainPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Check for existing email locally first
                val existing = repository.getUserByEmail(user.email)
                if (existing != null) {
                    _authState.value = AuthState.Error("Email already registered")
                    return@launch
                }
                // Create Firebase Auth account
                try {
                    FirebaseService.createAuthAccount(user.email, plainPassword)
                } catch (_: Exception) {
                    // Firebase unavailable — continue with local-only registration
                }
                val id = repository.registerUser(user)
                _authState.value = AuthState.Success(id)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        FirebaseService.signOut()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}
