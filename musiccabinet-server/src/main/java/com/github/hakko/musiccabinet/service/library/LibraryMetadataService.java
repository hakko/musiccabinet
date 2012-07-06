package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;

import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.File;

/*
 * The library scanning is modeled according to the "Pipes and Filters"
 * Enterprise Integration Pattern (EIP), and realized by Spring Integration.
 * 
 * This class acts as a filter that forces a physical meta data read of files
 * detected as new.
 */
public class LibraryMetadataService implements LibraryReceiverService {

	private PollableChannel libraryMetadataChannel; // consumer of
	private PollableChannel libraryAdditionChannel; // producer of

	private AudioTagService audioTagService;
	
	@SuppressWarnings("unchecked")
	@Override
	public void receive() {
		Message<DirectoryContent> message;
		while (true) {
			message = (Message<DirectoryContent>) libraryMetadataChannel.receive();
			if (message == null || message.equals(FINISHED_MESSAGE)) {
				libraryAdditionChannel.send(message);
				break;
			} else {
				for (File file : message.getPayload().getFiles()) {
					audioTagService.updateMetadata(file);
				}
				libraryAdditionChannel.send(message);
			}
		}
	}

	public void setLibraryMetadataChannel(PollableChannel libraryMetadataChannel) {
		this.libraryMetadataChannel = libraryMetadataChannel;
	}

	public void setLibraryAdditionChannel(PollableChannel libraryAdditionChannel) {
		this.libraryAdditionChannel = libraryAdditionChannel;
	}
	
	public void setAudioTagService(AudioTagService audioTagService) {
		this.audioTagService = audioTagService;
	}

}